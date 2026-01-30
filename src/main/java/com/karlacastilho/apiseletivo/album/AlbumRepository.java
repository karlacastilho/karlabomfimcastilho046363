package com.karlacastilho.apiseletivo.album;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Page<Album> findByArtists_Id(Long artistId, Pageable pageable);
}