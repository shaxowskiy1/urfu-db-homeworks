package ru.urfu.music.repository;

import ru.urfu.music.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {
    @EntityGraph(attributePaths = {"artist", "lyrics"})
    List<Track> findByGenreIgnoreCaseOrderByIdAsc(String genre);

    @Query("select t from Track t join fetch t.artist left join fetch t.lyrics where t.artist.id = :artistId order by t.id")
    List<Track> findByArtist(@Param("artistId") Long artistId);

    @Query(value = "select t.* from tracks t where t.duration_seconds <= :maxDuration order by t.duration_seconds, t.id", nativeQuery = true)
    List<Track> findShortTracks(@Param("maxDuration") int maxDuration);

    @EntityGraph(attributePaths = {"artist", "lyrics"})
    @Query("select t from Track t where t.id = :id")
    Optional<Track> findDetailedById(@Param("id") Long id);
}
