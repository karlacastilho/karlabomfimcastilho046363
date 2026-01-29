package com.karlacastilho.apiseletivo.artist;

import jakarta.validation.constraints.NotBlank;

public class ArtistCreateRequest {

    @NotBlank
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}