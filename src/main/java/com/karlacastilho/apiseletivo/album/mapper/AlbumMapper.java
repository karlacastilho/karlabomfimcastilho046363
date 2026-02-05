package com.karlacastilho.apiseletivo.album.mapper;

import com.karlacastilho.apiseletivo.album.dto.AlbumResponse;
import com.karlacastilho.apiseletivo.artist.dto.ArtistSummary;
import com.karlacastilho.apiseletivo.album.entity.Album;

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