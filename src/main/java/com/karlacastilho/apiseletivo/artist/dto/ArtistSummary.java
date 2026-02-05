package com.karlacastilho.apiseletivo.artist.dto;

import com.karlacastilho.apiseletivo.artist.entity.ArtistType;

public record ArtistSummary(
        Long id,
        String name,
        ArtistType type
) {}