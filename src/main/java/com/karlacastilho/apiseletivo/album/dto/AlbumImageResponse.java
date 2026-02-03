package com.karlacastilho.apiseletivo.album.dto;

import java.time.Instant;

public record AlbumImageResponse(
        Long id,
        String objectKey,
        Instant createdAt
) {}