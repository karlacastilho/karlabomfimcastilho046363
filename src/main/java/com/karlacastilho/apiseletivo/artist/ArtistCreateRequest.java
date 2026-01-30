package com.karlacastilho.apiseletivo.artist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class ArtistCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private ArtistType type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ArtistType getType() { return type; }
    public void setType(ArtistType type) { this.type = type; }
}