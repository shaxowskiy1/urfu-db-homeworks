package ru.urfu.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LyricsRequest(@NotBlank @Size(max = 16) String language,
                            @NotBlank @Size(max = 100000) String content) {
}
