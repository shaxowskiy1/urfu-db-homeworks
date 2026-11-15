package ru.urfu.music.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "playlists")
public class Playlist extends BaseEntity {
    @Column(nullable = false, length = 160)
    private String name;

    @OneToMany(mappedBy = "playlist", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PlaylistItem> items = new ArrayList<>();

    protected Playlist() {
    }

    public Playlist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<PlaylistItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(PlaylistItem item) {
        if (item.getPlaylist() != null && item.getPlaylist() != this) {
            throw new IllegalArgumentException("Item already belongs to another playlist");
        }
        if (!items.contains(item)) {
            items.add(item);
        }
        item.assignPlaylist(this);
    }

    public void removeItem(PlaylistItem item) {
        if (items.remove(item)) {
            item.assignPlaylist(null);
        }
    }
}
