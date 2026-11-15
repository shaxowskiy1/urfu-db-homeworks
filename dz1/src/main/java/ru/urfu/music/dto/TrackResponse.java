package ru.urfu.music.dto;

public record TrackResponse(Long id, String title, String genre, int durationSeconds,
                            ArtistResponse artist, LyricsResponse lyrics) {
}
