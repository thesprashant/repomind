package com.repomind.backend.repository;

public interface SearchResultProjection {
    String getFilePath();
    String getRawContent();
    Double getSimilarity();
}
