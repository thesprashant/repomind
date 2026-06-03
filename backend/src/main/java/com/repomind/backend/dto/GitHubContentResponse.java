package com.repomind.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubContentResponse(
    String name,
    String path,
    String sha,
    long size,
    String url,
    @JsonProperty("html_url") String htmlUrl,
    @JsonProperty("git_url") String gitUrl,
    @JsonProperty("download_url") String downloadUrl,
    String type
) {}
