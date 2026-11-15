package ru.urfu.music.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "lyrics")
public class Lyrics extends BaseEntity {
    @Column(nullable = false, length = 16)
    private String language;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    protected Lyrics() {
    }

    public Lyrics(String language, String content) {
        this.language = language;
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }
}
