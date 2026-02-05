package com.karlacastilho.apiseletivo.album.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AlbumUpdateRequest {

    @NotBlank
    private String title;

    @NotEmpty
    private List<Long> artistIds;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Long> getArtistIds() { return artistIds; }
    public void setArtistIds(List<Long> artistIds) { this.artistIds = artistIds; }
}