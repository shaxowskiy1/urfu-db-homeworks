package ru.urfu.music;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MusicApplicationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clearDatabase() {
        for (String table : new String[]{"playlist_items", "playlists", "tracks", "lyrics", "artists"}) {
            jdbc.update("delete from " + table);
        }
    }

    @Test
    void queriesRespectOptionalFiltersAndRelationships() throws Exception {
        long artist = createArtist("Artist A");
        long otherArtist = createArtist("Artist B");
        long first = createTrack(artist, "100% Night", "Rock", 180);
        createTrack(artist, "Dawn", "Jazz", 300);
        createTrack(otherArtist, "Night", "Rock", 120);

        mvc.perform(get("/api/tracks")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/tracks").param("title", "night").param("genre", "ROCK")
                        .param("artistId", Long.toString(artist)).param("minDuration", "150").param("maxDuration", "200"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(first)).andExpect(jsonPath("$[0].artist.name").value("Artist A"));
        mvc.perform(get("/api/tracks").param("title", "%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/tracks").param("title", " ").param("genre", " "))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/tracks/by-genre").param("genre", "rock"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/artists/{id}/tracks", artist))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/tracks/short").param("maxDuration", "180"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].durationSeconds").value(120));
        mvc.perform(get("/api/tracks").param("minDuration", "300").param("maxDuration", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lyricsArePersistedReplacedAndRemovedAsComposition() throws Exception {
        long artist = createArtist("Artist");
        JsonNode track = send(post("/api/tracks"), Map.of("title", "Song", "genre", "Rock",
                "durationSeconds", 180, "artistId", artist,
                "lyrics", Map.of("language", "en", "content", "Original words")), 201);
        long trackId = track.path("id").asLong();
        long oldLyrics = track.path("lyrics").path("id").asLong();
        mvc.perform(get("/api/tracks/{id}", trackId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.lyrics.content").value("Original words"));
        JsonNode replaced = send(put("/api/tracks/{id}/lyrics", trackId),
                Map.of("language", "ru", "content", "Новый текст"), 200);
        assertThat(replaced.path("lyrics").path("id").asLong()).isNotEqualTo(oldLyrics);
        assertThat(jdbc.queryForObject("select count(*) from lyrics where id = ?", Long.class, oldLyrics)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from lyrics", Long.class)).isEqualTo(1);
        mvc.perform(delete("/api/tracks/{id}/lyrics", trackId)).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from lyrics", Long.class)).isZero();
        mvc.perform(get("/api/tracks/{id}", trackId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.lyrics").isEmpty());
    }

    @Test
    void playlistOrdersItemsRejectsConflictsAndDetectsStaleVersions() throws Exception {
        long artist = createArtist("Artist");
        long first = createTrack(artist, "First", "Rock", 180);
        long second = createTrack(artist, "Second", "Rock", 200);
        long playlist = send(post("/api/playlists"), Map.of("name", "Favorites"), 201).path("id").asLong();
        JsonNode item = send(post("/api/playlists/{id}/items", playlist),
                Map.of("trackId", first, "position", 2), 201);
        long itemId = item.path("id").asLong();
        send(post("/api/playlists/{id}/items", playlist), Map.of("trackId", second, "position", 1), 201);
        long otherPlaylist = send(post("/api/playlists"), Map.of("name", "Another"), 201).path("id").asLong();
        send(post("/api/playlists/{id}/items", otherPlaylist), Map.of("trackId", first, "position", 1), 201);
        mvc.perform(get("/api/playlists/{id}", playlist)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].trackId").value(second))
                .andExpect(jsonPath("$.items[1].trackId").value(first));
        send(post("/api/playlists/{id}/items", playlist), Map.of("trackId", first, "position", 3), 409);
        send(patch("/api/playlists/{id}/items/{itemId}", playlist, itemId),
                Map.of("position", 1, "version", 0), 409);
        JsonNode moved = send(patch("/api/playlists/{id}/items/{itemId}", playlist, itemId),
                Map.of("position", 3, "version", 0), 200);
        assertThat(moved.path("version").asLong()).isEqualTo(1);
        send(patch("/api/playlists/{id}/items/{itemId}", playlist, itemId),
                Map.of("position", 4, "version", 0), 409);
        mvc.perform(delete("/api/playlists/{id}/items/{itemId}", playlist, itemId))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from playlist_items where id = ?", Long.class, itemId)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from tracks", Long.class)).isEqualTo(2);
    }

    @Test
    void validatesRequestsAndMissingResources() throws Exception {
        send(post("/api/artists"), Map.of("name", " "), 400);
        send(post("/api/tracks"), Map.of("title", "Bad", "genre", "Rock", "durationSeconds", 0, "artistId", 1), 400);
        send(post("/api/tracks"), Map.of("title", "Bad", "genre", "Rock", "durationSeconds", 100, "artistId", 9999), 404);
        mvc.perform(get("/api/tracks/{id}", 9999)).andExpect(status().isNotFound());
        mvc.perform(get("/api/playlists/{id}", 9999)).andExpect(status().isNotFound());
        mvc.perform(get("/api/tracks/short").param("maxDuration", "0")).andExpect(status().isBadRequest());
        long playlist = send(post("/api/playlists"), Map.of("name", "Empty"), 201).path("id").asLong();
        send(post("/api/playlists/{id}/items", playlist), Map.of("trackId", 9999, "position", 1), 404);
        mvc.perform(get("/api/playlists/{id}", playlist)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    private long createArtist(String name) throws Exception {
        return send(post("/api/artists"), Map.of("name", name), 201).path("id").asLong();
    }

    private long createTrack(long artist, String title, String genre, int duration) throws Exception {
        return send(post("/api/tracks"), Map.of("title", title, "genre", genre,
                "durationSeconds", duration, "artistId", artist), 201).path("id").asLong();
    }

    private JsonNode send(MockHttpServletRequestBuilder request, Object body, int expectedStatus) throws Exception {
        String response = mvc.perform(request.contentType("application/json").content(json.writeValueAsString(body)))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return response.isEmpty() ? json.nullNode() : json.readTree(response);
    }
}
