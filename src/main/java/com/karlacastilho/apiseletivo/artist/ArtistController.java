package com.karlacastilho.apiseletivo.artist;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
            Pageable pageable
    ) {
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
}