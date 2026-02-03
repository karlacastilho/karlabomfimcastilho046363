package com.karlacastilho.apiseletivo.artist.dto;

import com.karlacastilho.apiseletivo.artist.ArtistType;

public record ArtistResponse(
        Long id,
        String name,
        ArtistType type
) {}