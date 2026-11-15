package ru.urfu.music.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TrackRequest(@NotBlank @Size(max = 160) String title,
                           @NotBlank @Size(max = 60) String genre,
                           @Positive int durationSeconds,
                           @NotNull @Positive Long artistId,
                           @Valid LyricsRequest lyrics) {
}
