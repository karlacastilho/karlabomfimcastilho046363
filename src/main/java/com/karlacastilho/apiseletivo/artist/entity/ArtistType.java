package com.karlacastilho.apiseletivo.artist.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo do artista")
public enum ArtistType {
    @Schema(description = "Artista solo")
    CANTOR,
    @Schema(description = "Banda/grupo")
    BANDA
}