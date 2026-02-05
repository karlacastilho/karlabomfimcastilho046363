package com.karlacastilho.apiseletivo.artist.mapper;

import com.karlacastilho.apiseletivo.artist.dto.ArtistResponse;
import com.karlacastilho.apiseletivo.artist.entity.Artist;

public class ArtistMapper {
    public static ArtistResponse toResponse(Artist a) {
        return new ArtistResponse(a.getId(), a.getName(), a.getType());
    }
}