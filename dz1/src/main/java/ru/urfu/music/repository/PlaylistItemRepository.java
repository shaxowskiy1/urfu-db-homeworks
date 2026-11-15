package ru.urfu.music.repository;

import ru.urfu.music.domain.PlaylistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {
    @EntityGraph(attributePaths = {"track", "playlist"})
    Optional<PlaylistItem> findByIdAndPlaylistId(Long id, Long playlistId);
}
