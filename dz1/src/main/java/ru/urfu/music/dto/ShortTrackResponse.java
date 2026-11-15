package ru.urfu.music.dto;

public record ShortTrackResponse(Long id, String title, String genre, int durationSeconds, Long artistId) {
}
