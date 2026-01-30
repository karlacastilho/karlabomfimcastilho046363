package com.karlacastilho.apiseletivo.album;

import com.karlacastilho.apiseletivo.artist.Artist;
import com.karlacastilho.apiseletivo.artist.ArtistRepository;
import com.karlacastilho.apiseletivo.storage.AlbumCoverStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumRepository albumRepo;
    private final ArtistRepository artistRepo;
    private final AlbumCoverStorageService coverStorage;

    public AlbumController(AlbumRepository albumRepo,
                           ArtistRepository artistRepo,
                           AlbumCoverStorageService coverStorage) {
        this.albumRepo = albumRepo;
        this.artistRepo = artistRepo;
        this.coverStorage = coverStorage;
    }

    // -------------------------
    // CRUD
    // -------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Album create(@Valid @RequestBody AlbumCreateRequest req) {
        List<Artist> artists = artistRepo.findAllById(req.getArtistIds());
        if (artists.size() != req.getArtistIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais artistIds não existem");
        }

        Album album = new Album();
        album.setTitle(req.getTitle());

        // Salva o álbum primeiro para ter ID
        Album saved = albumRepo.save(album);

        // Como o dono da relação é Artist (@JoinTable fica no Artist),
        // persistimos o vínculo adicionando o álbum na coleção de cada artista
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

        album.setTitle(req.getTitle());

        // sincroniza os vínculos N:N com segurança
        syncArtists(album, req.getArtistIds());

        return albumRepo.save(album);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Album album = albumRepo.findWithArtistsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        // Remove vínculos na join table antes de deletar (evita FK)
        Set<Artist> currentArtists = new HashSet<>(album.getArtists());
        for (Artist a : currentArtists) {
            a.getAlbums().remove(album); // dono da relação
            album.getArtists().remove(a);
        }
        artistRepo.saveAll(currentArtists);

        albumRepo.delete(album);
    }

    // -------------------------
    // CAPA (MinIO)
    // -------------------------

    @PostMapping("/{id}/cover")
    public Album uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Album album = albumRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        String objectKey = coverStorage.uploadCover(id, file);
        album.setCoverObjectKey(objectKey);

        return albumRepo.save(album);
    }

    @GetMapping("/{id}/cover-url")
    public Map<String, String> getCoverUrl(@PathVariable Long id) {
        Album album = albumRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        if (album.getCoverObjectKey() == null || album.getCoverObjectKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não possui capa cadastrada");
        }

        String url = coverStorage.presignedGetUrl(album.getCoverObjectKey(), Duration.ofMinutes(30));
        return Map.of("url", url);
    }

    // -------------------------
    // Helper: sincronizar N:N
    // -------------------------

    private void syncArtists(Album album, List<Long> desiredArtistIds) {
        List<Artist> desiredArtistsList = artistRepo.findAllById(desiredArtistIds);
        if (desiredArtistsList.size() != desiredArtistIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais artistIds não existem");
        }

        Set<Artist> currentArtists = new HashSet<>(album.getArtists());
        Set<Artist> desiredArtists = new HashSet<>(desiredArtistsList);

        // Remover artistas que não devem mais estar vinculados
        for (Artist current : currentArtists) {
            if (!desiredArtists.contains(current)) {
                current.getAlbums().remove(album);      // dono da relação
                album.getArtists().remove(current);     // mantém consistência em memória
            }
        }

        // Adicionar artistas novos
        for (Artist desired : desiredArtists) {
            if (!album.getArtists().contains(desired)) {
                desired.getAlbums().add(album);         // dono da relação
                album.getArtists().add(desired);
            }
        }

        // Salva os artistas afetados (dono do relacionamento)
        artistRepo.saveAll(desiredArtists);
        artistRepo.saveAll(currentArtists);
    }
}