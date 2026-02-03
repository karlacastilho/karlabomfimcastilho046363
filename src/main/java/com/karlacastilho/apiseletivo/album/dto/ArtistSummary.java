package com.karlacastilho.apiseletivo.album.dto;

import com.karlacastilho.apiseletivo.artist.ArtistType;

public record ArtistSummary(
        Long id,
        String name,
        ArtistType type
) {}