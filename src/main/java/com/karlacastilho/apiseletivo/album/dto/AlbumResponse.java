package com.karlacastilho.apiseletivo.album.dto;

import com.karlacastilho.apiseletivo.artist.dto.ArtistSummary;

import java.util.List;

public record AlbumResponse(
        Long id,
        String title,
        List<ArtistSummary> artists
) {}