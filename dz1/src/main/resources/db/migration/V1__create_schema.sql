CREATE SEQUENCE entity_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE artists (
    id BIGINT PRIMARY KEY,
    name VARCHAR(120) NOT NULL
);

CREATE TABLE lyrics (
    id BIGINT PRIMARY KEY,
    language VARCHAR(16) NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE tracks (
    id BIGINT PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    genre VARCHAR(60) NOT NULL,
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds > 0),
    artist_id BIGINT NOT NULL REFERENCES artists(id),
    lyrics_id BIGINT UNIQUE REFERENCES lyrics(id)
);

CREATE TABLE playlists (
    id BIGINT PRIMARY KEY,
    name VARCHAR(160) NOT NULL
);

CREATE TABLE playlist_items (
    id BIGINT PRIMARY KEY,
    playlist_id BIGINT NOT NULL REFERENCES playlists(id),
    track_id BIGINT NOT NULL REFERENCES tracks(id),
    position INTEGER NOT NULL CHECK (position > 0),
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (playlist_id, track_id),
    UNIQUE (playlist_id, position)
);

CREATE INDEX idx_tracks_artist ON tracks(artist_id);
CREATE INDEX idx_tracks_genre ON tracks(genre);
CREATE INDEX idx_playlist_items_track ON playlist_items(track_id);
