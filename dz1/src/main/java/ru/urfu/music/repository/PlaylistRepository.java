package ru.urfu.music.repository;

import ru.urfu.music.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    @Query("select distinct p from Playlist p left join fetch p.items i left join fetch i.track where p.id = :id")
    Optional<Playlist> findDetailedById(@Param("id") Long id);
}
