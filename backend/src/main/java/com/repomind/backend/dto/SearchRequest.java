package com.repomind.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchRequest(
    String query, 
    String repo, 
    @JsonProperty("top_k") Integer topK
) {}
