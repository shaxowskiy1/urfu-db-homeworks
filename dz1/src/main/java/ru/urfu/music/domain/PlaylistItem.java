package ru.urfu.music.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"playlist_id", "track_id"}),
        @UniqueConstraint(columnNames = {"playlist_id", "position"})
})
public class PlaylistItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(nullable = false)
    private int position;

    @Version
    @Column(nullable = false)
    private long version;

    protected PlaylistItem() {
    }

    public PlaylistItem(Playlist playlist, Track track, int position) {
        this.track = track;
        this.position = position;
        playlist.addItem(this);
    }

    void assignPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public Track getTrack() {
        return track;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getVersion() {
        return version;
    }
}
