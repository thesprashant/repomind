package com.repomind.backend.controller;

import com.repomind.backend.dto.SearchRequest;
import com.repomind.backend.dto.SearchResponse;
import com.repomind.backend.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repomind")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<SearchResponse>> search(@RequestBody SearchRequest request) {
        int limit = request.topK() != null ? request.topK() : 5;
        var results = searchService.semanticSearch(request.query(), request.repo(), limit);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Backend is ALIVE and AWS CI/CD Watchtower deployment was a SUCCESS!");
    }
}
