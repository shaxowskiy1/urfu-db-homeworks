package ru.urfu.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtistRequest(@NotBlank @Size(max = 120) String name) {
}
