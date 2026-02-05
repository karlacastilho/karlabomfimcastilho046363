package com.karlacastilho.apiseletivo.album.mapper;

import com.karlacastilho.apiseletivo.album.dto.AlbumImageResponse;
import com.karlacastilho.apiseletivo.album.entity.AlbumImage;

public class AlbumImageMapper {
    public static AlbumImageResponse toResponse(AlbumImage img) {
        return new AlbumImageResponse(img.getId(), img.getObjectKey(), img.getCreatedAt());
    }
}