package ru.urfu.music.dto;

import java.util.List;

public record PlaylistResponse(Long id, String name, List<ItemResponse> items) {
}
