package ru.urfu.music.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "artists")
public class Artist extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY)
    private List<Track> tracks = new ArrayList<>();

    protected Artist() {
    }

    public Artist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public void addTrack(Track track) {
        if (track.getArtist() != null && track.getArtist() != this) {
            track.getArtist().tracks.remove(track);
        }
        if (!tracks.contains(track)) {
            tracks.add(track);
        }
        track.assignArtist(this);
    }
}
