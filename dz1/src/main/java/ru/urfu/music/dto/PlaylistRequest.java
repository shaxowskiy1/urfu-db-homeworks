package ru.urfu.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistRequest(@NotBlank @Size(max = 160) String name) {
}
