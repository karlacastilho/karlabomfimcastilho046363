package com.karlacastilho.apiseletivo.artist.repository;

import com.karlacastilho.apiseletivo.artist.entity.ArtistType;
import com.karlacastilho.apiseletivo.artist.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Artist> findByType(ArtistType type, Pageable pageable);
    Page<Artist> findByTypeAndNameContainingIgnoreCase(ArtistType type, String name, Pageable pageable);

}