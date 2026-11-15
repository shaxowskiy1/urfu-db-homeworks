package ru.urfu.music.dto;

public record ItemResponse(Long id, Long trackId, String title, int position, long version) {
}
