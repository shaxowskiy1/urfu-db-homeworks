package ru.urfu.music.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tracks")
public class Track extends BaseEntity {
    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 60)
    private String genre;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinColumn(name = "lyrics_id", unique = true)
    private Lyrics lyrics;

    protected Track() {
    }

    public Track(String title, String genre, int durationSeconds, Artist artist, Lyrics lyrics) {
        this.title = title;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
        this.lyrics = lyrics;
        artist.addTrack(this);
    }

    void assignArtist(Artist artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public Artist getArtist() {
        return artist;
    }

    public Lyrics getLyrics() {
        return lyrics;
    }

    public void setLyrics(Lyrics lyrics) {
        this.lyrics = lyrics;
    }
}
