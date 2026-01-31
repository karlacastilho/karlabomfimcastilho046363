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
import org.springframework.http.MediaType;
import com.karlacastilho.apiseletivo.album.AlbumImage;
import com.karlacastilho.apiseletivo.album.AlbumImageRepository;
import org.springframework.http.MediaType;
import java.util.stream.Collectors;


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
    private final AlbumImageRepository albumImageRepo;


    public AlbumController(AlbumRepository albumRepo,
                           ArtistRepository artistRepo,
                           AlbumCoverStorageService coverStorage,
                           AlbumImageRepository albumImageRepo) {
        this.albumRepo = albumRepo;
        this.artistRepo = artistRepo;
        this.coverStorage = coverStorage;
        this.albumImageRepo = albumImageRepo;
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

    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Album updateCover(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
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

    // PUT /api/v1/albums/{id}/images (multipart, vários arquivos)
    @PutMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<AlbumImage> uploadImages(
            @PathVariable Long id,
            @RequestPart("files") List<MultipartFile> files
    ) {
        Album album = albumRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie pelo menos 1 arquivo em 'files'");
        }

        return files.stream().map(f -> {
            String objectKey = coverStorage.uploadAlbumImage(id, f);

            AlbumImage img = new AlbumImage();
            img.setAlbum(album);
            img.setObjectKey(objectKey);
            return albumImageRepo.save(img);
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}/images")
    public List<AlbumImage> listImages(@PathVariable Long id) {
        albumRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        return albumImageRepo.findByAlbum_Id(id);
    }

    @GetMapping("/{id}/images/{imageId}/url")
    public Map<String, String> getImageUrl(@PathVariable Long id, @PathVariable Long imageId) {
        AlbumImage img = albumImageRepo.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada"));

        if (!img.getAlbum().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem não pertence ao álbum informado");
        }

        String url = coverStorage.presignedGetUrl(img.getObjectKey(), Duration.ofMinutes(30));
        return Map.of("url", url);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        AlbumImage img = albumImageRepo.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada"));

        if (!img.getAlbum().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem não pertence ao álbum informado");
        }

        albumImageRepo.delete(img);
        }
    }