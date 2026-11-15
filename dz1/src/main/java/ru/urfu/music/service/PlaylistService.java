package ru.urfu.music.service;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urfu.music.dto.PlaylistRequest;
import ru.urfu.music.dto.ItemRequest;
import ru.urfu.music.dto.PositionRequest;
import ru.urfu.music.dto.ItemResponse;
import ru.urfu.music.dto.PlaylistResponse;
import ru.urfu.music.dto.PlaylistSummary;
import ru.urfu.music.domain.Playlist;
import ru.urfu.music.domain.PlaylistItem;
import ru.urfu.music.domain.Track;
import ru.urfu.music.repository.PlaylistItemRepository;
import ru.urfu.music.repository.PlaylistRepository;
import ru.urfu.music.repository.TrackRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class PlaylistService {
    private final PlaylistRepository playlists;
    private final PlaylistItemRepository items;
    private final TrackRepository tracks;

    public PlaylistService(PlaylistRepository playlists, PlaylistItemRepository items, TrackRepository tracks) {
        this.playlists = playlists;
        this.items = items;
        this.tracks = tracks;
    }

    @Transactional
    public PlaylistResponse create(PlaylistRequest request) {
        return response(playlists.save(new Playlist(request.name().trim())));
    }

    @Transactional(readOnly = true)
    public List<PlaylistSummary> list() {
        return playlists.findAll(Sort.by("id")).stream()
                .map(p -> new PlaylistSummary(p.getId(), p.getName())).toList();
    }

    @Transactional(readOnly = true)
    public PlaylistResponse get(Long id) {
        return response(requirePlaylist(id));
    }

    @Transactional
    public ItemResponse add(Long playlistId, ItemRequest request) {
        Playlist playlist = requirePlaylist(playlistId);
        Track track = tracks.findById(request.trackId())
                .orElseThrow(() -> CatalogService.missing("Track", request.trackId()));
        if (playlist.getItems().stream().anyMatch(i -> i.getTrack().getId().equals(request.trackId()))) {
            throw conflict("Track already exists in playlist");
        }
        requireFreePosition(playlist, request.position(), null);
        PlaylistItem item = new PlaylistItem(playlist, track, request.position());
        items.saveAndFlush(item);
        return itemResponse(item);
    }

    @Transactional
    public ItemResponse move(Long playlistId, Long itemId, PositionRequest request) {
        PlaylistItem item = items.findByIdAndPlaylistId(itemId, playlistId)
                .orElseThrow(() -> CatalogService.missing("Playlist item", itemId));
        if (item.getVersion() != request.version().longValue()) {
            throw conflict("Playlist item was modified; reload its version");
        }
        requireFreePosition(requirePlaylist(playlistId), request.position(), itemId);
        item.setPosition(request.position());
        items.flush();
        return itemResponse(item);
    }

    @Transactional
    public void remove(Long playlistId, Long itemId) {
        Playlist playlist = requirePlaylist(playlistId);
        PlaylistItem item = playlist.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> CatalogService.missing("Playlist item", itemId));
        playlist.removeItem(item);
    }

    private Playlist requirePlaylist(Long id) {
        return playlists.findDetailedById(id).orElseThrow(() -> CatalogService.missing("Playlist", id));
    }

    private void requireFreePosition(Playlist playlist, int position, Long excludedId) {
        if (playlist.getItems().stream().anyMatch(i -> i.getPosition() == position && !i.getId().equals(excludedId))) {
            throw conflict("Position is already occupied");
        }
    }

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private static PlaylistResponse response(Playlist playlist) {
        return new PlaylistResponse(playlist.getId(), playlist.getName(), playlist.getItems().stream()
                .sorted(Comparator.comparingInt(PlaylistItem::getPosition)).map(PlaylistService::itemResponse).toList());
    }

    private static ItemResponse itemResponse(PlaylistItem item) {
        return new ItemResponse(item.getId(), item.getTrack().getId(), item.getTrack().getTitle(),
                item.getPosition(), item.getVersion());
    }
}
