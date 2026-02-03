package com.karlacastilho.apiseletivo.album.dto;

import com.karlacastilho.apiseletivo.album.AlbumImage;

public class AlbumImageMapper {
    public static AlbumImageResponse toResponse(AlbumImage img) {
        return new AlbumImageResponse(img.getId(), img.getObjectKey(), img.getCreatedAt());
    }
}