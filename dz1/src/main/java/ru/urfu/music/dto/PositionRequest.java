package ru.urfu.music.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PositionRequest(@Positive int position, @NotNull @PositiveOrZero Long version) {
}
