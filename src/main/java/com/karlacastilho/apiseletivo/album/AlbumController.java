package com.karlacastilho.apiseletivo.album;

import com.karlacastilho.apiseletivo.artist.Artist;
import com.karlacastilho.apiseletivo.artist.ArtistRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumRepository albumRepo;
    private final ArtistRepository artistRepo;

    public AlbumController(AlbumRepository albumRepo, ArtistRepository artistRepo) {
        this.albumRepo = albumRepo;
        this.artistRepo = artistRepo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Album create(@Valid @RequestBody AlbumCreateRequest req) {

        List<Artist> artists = artistRepo.findAllById(req.getArtistIds());
        if (artists.size() != req.getArtistIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais artistIds não existem");
        }

        Album album = new Album();
        album.setTitle(req.getTitle());

        // salvar primeiro para garantir ID
        Album saved = albumRepo.save(album);

        // vincular N:N pelo lado do Artist (dono do relacionamento)
        Set<Album> singleton = new HashSet<>();
        singleton.add(saved);

        for (Artist a : artists) {
            a.getAlbums().add(saved);
        }
        artistRepo.saveAll(artists);

        return saved;
    }

    @GetMapping
    public Page<Album> list(@RequestParam(required = false) Long artistId, Pageable pageable) {
        if (artistId != null) {
            return albumRepo.findByArtists_Id(artistId, pageable);
        }
        return albumRepo.findAll(pageable);
    }
}