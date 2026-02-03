package com.karlacastilho.apiseletivo.album.dto;

import java.util.List;

public record AlbumResponse(
        Long id,
        String title,
        List<ArtistSummary> artists
) {}