package ru.urfu.music.repository;

import ru.urfu.music.domain.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LyricsRepository extends JpaRepository<Lyrics, Long> {
}
