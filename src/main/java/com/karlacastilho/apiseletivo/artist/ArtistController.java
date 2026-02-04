package com.karlacastilho.apiseletivo.artist;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.karlacastilho.apiseletivo.artist.dto.ArtistResponse;
import com.karlacastilho.apiseletivo.artist.dto.ArtistMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Artists", description = "Operações de cadastro e consulta de artistas")
@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistRepository repo;

    public ArtistController(ArtistRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Criar artista", description = "Cria um artista (CANTOR ou BANDA).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artista criado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistResponse create(@Valid @RequestBody ArtistCreateRequest req) {
        Artist a = new Artist();
        a.setName(req.getName());
        a.setType(req.getType());
        return ArtistMapper.toResponse(repo.save(a));
    }

    @Operation(summary = "Listar artistas", description = "Lista artistas com paginação. Filtros opcionais por nome e tipo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping
    public Page<ArtistResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ArtistType type,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
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

        // converte Artist -> ArtistResponse (sem albums)
        return page.map(a -> new ArtistResponse(a.getId(), a.getName(), a.getType()));
    }

    @Operation(summary = "Atualizar artista", description = "Atualiza nome e tipo do artista pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista atualizado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PutMapping("/{id}")
    public ArtistResponse update(
            @Parameter(description = "ID do artista", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ArtistUpdateRequest req
    ) {
        Artist artist = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        artist.setName(req.getName());
        artist.setType(req.getType());

        return ArtistMapper.toResponse(repo.save(artist));
    }

    @Operation(summary = "Remover artista", description = "Remove um artista pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artista removido"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "ID do artista", example = "1")
            @PathVariable Long id
    ) {
        Artist artist = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        artist.getAlbums().clear(); // remove vínculos N:N na join table
        repo.save(artist);

        repo.delete(artist);
    }
}