package ru.urfu.music.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.urfu.music.dto.ItemRequest;
import ru.urfu.music.dto.ItemResponse;
import ru.urfu.music.dto.PositionRequest;
import ru.urfu.music.service.PlaylistService;

@RestController
@RequestMapping("/api/playlists/{id}/items")
@Validated
public class PlaylistItemController {
    private final PlaylistService service;

    public PlaylistItemController(PlaylistService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse add(@PathVariable @Positive Long id, @Valid @RequestBody ItemRequest request) {
        return service.add(id, request);
    }

    @PatchMapping("/{itemId}")
    public ItemResponse move(@PathVariable @Positive Long id, @PathVariable @Positive Long itemId,
                             @Valid @RequestBody PositionRequest request) {
        return service.move(id, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable @Positive Long id, @PathVariable @Positive Long itemId) {
        service.remove(id, itemId);
    }
}
