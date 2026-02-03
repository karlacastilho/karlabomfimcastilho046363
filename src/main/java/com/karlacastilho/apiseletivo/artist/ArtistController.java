package com.karlacastilho.apiseletivo.artist;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.karlacastilho.apiseletivo.artist.dto.ArtistResponse;
import com.karlacastilho.apiseletivo.artist.dto.ArtistMapper;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistRepository repo;

    public ArtistController(ArtistRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistResponse create(@Valid @RequestBody ArtistCreateRequest req) {
        Artist a = new Artist();
        a.setName(req.getName());
        a.setType(req.getType());
        return ArtistMapper.toResponse(repo.save(a));
    }

    @GetMapping
    public Page<ArtistResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ArtistType type,
            Pageable pageable
    ) {
        boolean hasName = name != null && !name.isBlank();

        Page<Artist> page;

        if (type != null && hasName) {
            page = repo.findByTypeAndNameContainingIgnoreCase(type, name, pageable);
        } else if (type != null) {
            page = repo.findByType(type, pageable);
        } else if (hasName) {
            page = repo.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = repo.findAll(pageable);
        }

        // converte Artist -> ArtistResponse (não retorna albums)
        return page.map(a -> new ArtistResponse(a.getId(), a.getName(), a.getType()));
    }

    @PutMapping("/{id}")
    public ArtistResponse update(@PathVariable Long id, @Valid @RequestBody ArtistUpdateRequest req) {
        Artist artist = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        artist.setName(req.getName());
        artist.setType(req.getType());

        return ArtistMapper.toResponse(repo.save(artist));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Artist artist = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        artist.getAlbums().clear(); // remove vínculos N:N na join table
        repo.save(artist);

        repo.delete(artist);
    }
}