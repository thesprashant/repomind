package com.repomind.backend.service;

import com.repomind.backend.dto.SearchResponse;
import com.repomind.backend.repository.CodeEmbeddingRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final CodeEmbeddingRepository repository;
    private final EmbeddingService embeddingService;

    public SearchService(CodeEmbeddingRepository repository, EmbeddingService embeddingService) {
        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    public List<SearchResponse> semanticSearch(String query, String repo, int topK) {
        float[] queryVector = embeddingService.generateEmbedding(query);
        if (queryVector == null) {
            return List.of();
        }
        
        // Convert float array to string representation "[0.1, 0.2, ...]" for safe native query binding
        String vectorStr = Arrays.toString(queryVector);
        
        var results = repository.findSimilarCodeChunks(repo, vectorStr, topK);
        
        return results.stream()
                .map(proj -> new SearchResponse(proj.getFilePath(), proj.getRawContent(), proj.getSimilarity()))
                .collect(Collectors.toList());
    }
}
