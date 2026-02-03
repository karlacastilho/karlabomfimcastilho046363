package com.karlacastilho.apiseletivo.album.dto;

import com.karlacastilho.apiseletivo.album.Album;

public class AlbumMapper {
    public static AlbumResponse toResponse(Album album) {
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getArtists().stream()
                        .map(a -> new ArtistSummary(a.getId(), a.getName(), a.getType()))
                        .toList()
        );
    }
}