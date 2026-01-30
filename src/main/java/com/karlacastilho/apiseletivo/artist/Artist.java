package com.karlacastilho.apiseletivo.artist;

import jakarta.persistence.*;
import com.karlacastilho.apiseletivo.album.Album;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @ManyToMany
    @JoinTable(
            name = "artist_album",
            joinColumns = @JoinColumn(name = "artist_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    private Set<Album> albums = new HashSet<>();

    public Set<Album> getAlbums() { return albums; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtistType type = ArtistType.BANDA;

    public ArtistType getType() { return type; }
    public void setType(ArtistType type) { this.type = type; }
}