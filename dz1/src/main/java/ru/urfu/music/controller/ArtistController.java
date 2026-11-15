package ru.urfu.music.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.urfu.music.dto.ArtistRequest;
import ru.urfu.music.dto.ArtistResponse;
import ru.urfu.music.dto.TrackResponse;
import ru.urfu.music.service.CatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@Validated
public class ArtistController {
    private final CatalogService service;

    public ArtistController(CatalogService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistResponse createArtist(@Valid @RequestBody ArtistRequest request) {
        return service.createArtist(request);
    }

    @GetMapping
    public List<ArtistResponse> artists() {
        return service.artists();
    }

    @GetMapping("/{id}/tracks")
    public List<TrackResponse> byArtist(@PathVariable @Positive Long id) {
        return service.byArtist(id);
    }
}
