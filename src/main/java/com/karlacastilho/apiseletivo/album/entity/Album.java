package com.karlacastilho.apiseletivo.album.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.karlacastilho.apiseletivo.artist.entity.Artist;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "cover_object_key")
    private String coverObjectKey;

    @JsonIgnore
    @ManyToMany(mappedBy = "albums")
    private Set<Artist> artists = new HashSet<>();

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverObjectKey() { return coverObjectKey; }
    public void setCoverObjectKey(String coverObjectKey) { this.coverObjectKey = coverObjectKey; }

    public Set<Artist> getArtists() { return artists; }

    public void addArtist(Artist artist) {
        this.artists.add(artist);
        artist.getAlbums().add(this);
    }

    public void removeArtist(Artist artist) {
        this.artists.remove(artist);
        artist.getAlbums().remove(this);
    }
}