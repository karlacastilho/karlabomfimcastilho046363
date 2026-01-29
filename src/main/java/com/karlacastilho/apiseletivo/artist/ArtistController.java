package com.karlacastilho.apiseletivo.artist;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return repo.save(a);
    }

    @GetMapping
    public Page<Artist> list(@RequestParam(required = false) String name, Pageable pageable) {
        if (name != null && !name.isBlank()) {
            return repo.findByNameContainingIgnoreCase(name, pageable);
        }
        return repo.findAll(pageable);
    }
}