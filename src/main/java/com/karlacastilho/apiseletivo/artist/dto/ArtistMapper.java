package com.karlacastilho.apiseletivo.artist.dto;

import com.karlacastilho.apiseletivo.artist.Artist;

public class ArtistMapper {
    public static ArtistResponse toResponse(Artist a) {
        return new ArtistResponse(a.getId(), a.getName(), a.getType());
    }
}