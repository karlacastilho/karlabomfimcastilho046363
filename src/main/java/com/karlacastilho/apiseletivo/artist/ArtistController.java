package com.karlacastilho.apiseletivo.artist;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistRepository repo;

    public ArtistController(ArtistRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Artist create(@Valid @RequestBody ArtistCreateRequest req) {
        Artist a = new Artist();
        a.setName(req.getName());
        a.setType(req.getType());
        return repo.save(a);
    }

    @GetMapping
    public Page<Artist> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ArtistType type,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        // validar campos permitidos para sort
        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            if (!property.equals("id") && !property.equals("name") && !property.equals("type")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenação inválido: " + property);
            }
        });

        boolean hasName = name != null && !name.isBlank();

        if (type != null && hasName) {
            return repo.findByTypeAndNameContainingIgnoreCase(type, name, pageable);
        }
        if (type != null) {
            return repo.findByType(type, pageable);
        }
        if (hasName) {
            return repo.findByNameContainingIgnoreCase(name, pageable);
        }
        return repo.findAll(pageable);
    }

    @PutMapping("/{id}")
    public Artist update(@PathVariable Long id, @Valid @RequestBody ArtistUpdateRequest req) {
        Artist artist = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        artist.setName(req.getName());
        artist.setType(req.getType());

        return repo.save(artist);
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