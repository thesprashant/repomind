package com.repomind.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchResponse(
    @JsonProperty("file_path") String filePath,
    String content,
    Double similarity
) {}
