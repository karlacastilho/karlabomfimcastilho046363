package com.karlacastilho.apiseletivo.album;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByArtists_Id(Long artistId, Pageable pageable);

    @EntityGraph(attributePaths = "artists")
    Optional<Album> findWithArtistsById(Long id);
}