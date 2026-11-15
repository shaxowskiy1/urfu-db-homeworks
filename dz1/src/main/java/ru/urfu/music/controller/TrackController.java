package ru.urfu.music.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.urfu.music.dto.ShortTrackResponse;
import ru.urfu.music.dto.TrackRequest;
import ru.urfu.music.dto.TrackResponse;
import ru.urfu.music.service.CatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/tracks")
@Validated
public class TrackController {
    private final CatalogService service;

    public TrackController(CatalogService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackResponse createTrack(@Valid @RequestBody TrackRequest request) {
        return service.createTrack(request);
    }

    @GetMapping("/{id}")
    public TrackResponse track(@PathVariable @Positive Long id) {
        return service.track(id);
    }

    @GetMapping("/by-genre")
    public List<TrackResponse> byGenre(@RequestParam @NotBlank String genre) {
        return service.byGenre(genre);
    }

    @GetMapping("/short")
    public List<ShortTrackResponse> shortTracks(@RequestParam @Positive int maxDuration) {
        return service.shortTracks(maxDuration);
    }

    @GetMapping
    public List<TrackResponse> search(@RequestParam(required = false) String title,
                                      @RequestParam(required = false) String genre,
                                      @RequestParam(required = false) @Positive Long artistId,
                                      @RequestParam(required = false) @Positive Integer minDuration,
                                      @RequestParam(required = false) @Positive Integer maxDuration) {
        return service.search(title, genre, artistId, minDuration, maxDuration);
    }
}
