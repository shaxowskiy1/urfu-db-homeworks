package ru.urfu.music.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.urfu.music.dto.PlaylistRequest;
import ru.urfu.music.dto.PlaylistResponse;
import ru.urfu.music.dto.PlaylistSummary;
import ru.urfu.music.service.PlaylistService;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@Validated
public class PlaylistController {
    private final PlaylistService service;

    public PlaylistController(PlaylistService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse create(@Valid @RequestBody PlaylistRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<PlaylistSummary> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public PlaylistResponse get(@PathVariable @Positive Long id) {
        return service.get(id);
    }
}
