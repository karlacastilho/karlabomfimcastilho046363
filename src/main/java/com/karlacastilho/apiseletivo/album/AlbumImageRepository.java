package com.karlacastilho.apiseletivo.album;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumImageRepository extends JpaRepository<AlbumImage, Long> {
    List<AlbumImage> findByAlbum_Id(Long albumId);
}