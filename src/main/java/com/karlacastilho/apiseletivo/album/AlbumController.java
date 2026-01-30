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
        Album saved = albumRepo.save(album);

        // vincular N:N: atualizar pelo lado de Artist (dono do relacionamento)
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

    @PutMapping("/{id}")
    public Album update(@PathVariable Long id, @Valid @RequestBody AlbumUpdateRequest req) {
        Album album = albumRepo.findWithArtistsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        // Atualiza título
        album.setTitle(req.getTitle());

        // Atualiza vínculos com artistas
        syncArtists(album, req.getArtistIds());

        return albumRepo.save(album);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Album album = albumRepo.findWithArtistsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        // remover relações N:N antes de deletar o álbum (evita constraint na artist_album)
        for (Artist a : new HashSet<>(album.getArtists())) {
            a.getAlbums().remove(album);
        }
        artistRepo.saveAll(album.getArtists());

        albumRepo.delete(album);
    }

    private void syncArtists(Album album, List<Long> artistIds) {
        List<Artist> newArtists = artistRepo.findAllById(artistIds);
        if (newArtists.size() != artistIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais artistIds não existem");
        }

        Set<Artist> current = album.getArtists();
        Set<Artist> desired = new HashSet<>(newArtists);

        // remove os que não devem mais estar
        for (Artist a : new HashSet<>(current)) {
            if (!desired.contains(a)) {
                a.getAlbums().remove(album);
                current.remove(a);
            }
        }

        // adiciona os novos
        for (Artist a : desired) {
            if (!current.contains(a)) {
                a.getAlbums().add(album);
                current.add(a);
            }
        }

        artistRepo.saveAll(desired);
    }
}