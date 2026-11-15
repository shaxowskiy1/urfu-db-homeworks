package ru.urfu.music.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.urfu.music.dto.LyricsRequest;
import ru.urfu.music.dto.TrackResponse;
import ru.urfu.music.service.CatalogService;

@RestController
@RequestMapping("/api/tracks/{id}/lyrics")
@Validated
public class LyricsController {
    private final CatalogService service;

    public LyricsController(CatalogService service) {
        this.service = service;
    }

    @PutMapping
    public TrackResponse replaceLyrics(@PathVariable @Positive Long id, @Valid @RequestBody LyricsRequest request) {
        return service.replaceLyrics(id, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLyrics(@PathVariable @Positive Long id) {
        service.deleteLyrics(id);
    }
}
