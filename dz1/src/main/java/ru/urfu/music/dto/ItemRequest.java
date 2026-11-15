package ru.urfu.music.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemRequest(@NotNull @Positive Long trackId, @Positive int position) {
}
