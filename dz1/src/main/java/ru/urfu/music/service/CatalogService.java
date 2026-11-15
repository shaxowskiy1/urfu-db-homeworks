package ru.urfu.music.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urfu.music.dto.ArtistRequest;
import ru.urfu.music.dto.LyricsRequest;
import ru.urfu.music.dto.TrackRequest;
import ru.urfu.music.dto.ArtistResponse;
import ru.urfu.music.dto.LyricsResponse;
import ru.urfu.music.dto.TrackResponse;
import ru.urfu.music.dto.ShortTrackResponse;
import ru.urfu.music.domain.*;
import ru.urfu.music.repository.ArtistRepository;
import ru.urfu.music.repository.TrackRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CatalogService {
    private final ArtistRepository artists;
    private final TrackRepository tracks;
    private final EntityManager entityManager;

    public CatalogService(ArtistRepository artists, TrackRepository tracks, EntityManager entityManager) {
        this.artists = artists;
        this.tracks = tracks;
        this.entityManager = entityManager;
    }

    @Transactional
    public ArtistResponse createArtist(ArtistRequest request) {
        return artistResponse(artists.save(new Artist(request.name().trim())));
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> artists() {
        return artists.findAll(Sort.by("id")).stream().map(CatalogService::artistResponse).toList();
    }

    @Transactional
    public TrackResponse createTrack(TrackRequest request) {
        Artist artist = artists.findById(request.artistId())
                .orElseThrow(() -> missing("Artist", request.artistId()));
        Track track = new Track(request.title().trim(), request.genre().trim(), request.durationSeconds(),
                artist, request.lyrics() == null ? null : lyrics(request.lyrics()));
        return trackResponse(tracks.save(track));
    }

    @Transactional(readOnly = true)
    public TrackResponse track(Long id) {
        return trackResponse(requireTrack(id));
    }

    @Transactional(readOnly = true)
    public List<TrackResponse> byArtist(Long artistId) {
        if (!artists.existsById(artistId)) {
            throw missing("Artist", artistId);
        }
        return tracks.findByArtist(artistId).stream().map(CatalogService::trackResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TrackResponse> byGenre(String genre) {
        return tracks.findByGenreIgnoreCaseOrderByIdAsc(genre.trim())
                .stream().map(CatalogService::trackResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ShortTrackResponse> shortTracks(int maxDuration) {
        return tracks.findShortTracks(maxDuration).stream()
                .map(t -> new ShortTrackResponse(t.getId(), t.getTitle(), t.getGenre(),
                        t.getDurationSeconds(), t.getArtist().getId())).toList();
    }

    @Transactional(readOnly = true)
    public List<TrackResponse> search(String title, String genre, Long artistId,
                                      Integer minDuration, Integer maxDuration) {
        if (minDuration != null && maxDuration != null && minDuration > maxDuration) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minDuration must not exceed maxDuration");
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Track> query = builder.createQuery(Track.class);
        Root<Track> root = query.from(Track.class);
        root.fetch("artist", JoinType.INNER);
        root.fetch("lyrics", JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();
        if (title != null && !title.isBlank()) {
            String pattern = title.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            predicates.add(builder.like(builder.lower(root.get("title")), "%" + pattern + "%", '\\'));
        }
        if (genre != null && !genre.isBlank()) {
            predicates.add(builder.equal(builder.lower(root.get("genre")), genre.trim().toLowerCase(Locale.ROOT)));
        }
        if (artistId != null) {
            predicates.add(builder.equal(root.get("artist").get("id"), artistId));
        }
        if (minDuration != null) {
            predicates.add(builder.ge(root.get("durationSeconds"), minDuration));
        }
        if (maxDuration != null) {
            predicates.add(builder.le(root.get("durationSeconds"), maxDuration));
        }
        query.select(root).where(predicates.toArray(Predicate[]::new)).orderBy(builder.asc(root.get("id")));
        return entityManager.createQuery(query).getResultList().stream().map(CatalogService::trackResponse).toList();
    }

    @Transactional
    public TrackResponse replaceLyrics(Long trackId, LyricsRequest request) {
        Track track = requireTrack(trackId);
        track.setLyrics(lyrics(request));
        tracks.flush();
        return trackResponse(track);
    }

    @Transactional
    public void deleteLyrics(Long trackId) {
        requireTrack(trackId).setLyrics(null);
    }

    private Track requireTrack(Long id) {
        return tracks.findDetailedById(id).orElseThrow(() -> missing("Track", id));
    }

    private static Lyrics lyrics(LyricsRequest request) {
        return new Lyrics(request.language().trim(), request.content());
    }

    private static ArtistResponse artistResponse(Artist artist) {
        return new ArtistResponse(artist.getId(), artist.getName());
    }

    private static TrackResponse trackResponse(Track track) {
        Lyrics text = track.getLyrics();
        return new TrackResponse(track.getId(), track.getTitle(), track.getGenre(), track.getDurationSeconds(),
                artistResponse(track.getArtist()), text == null ? null
                : new LyricsResponse(text.getId(), text.getLanguage(), text.getContent()));
    }

    static ResponseStatusException missing(String entity, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, entity + " " + id + " not found");
    }
}
