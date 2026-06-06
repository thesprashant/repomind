package com.repomind.backend.service;

import com.repomind.backend.entity.CodeEmbedding;
import com.repomind.backend.repository.CodeEmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final GitHubClientService gitHubClientService;
    private final EmbeddingService embeddingService;
    private final CodeEmbeddingRepository repository;

    public IngestionService(GitHubClientService gitHubClientService, 
                            EmbeddingService embeddingService, 
                            CodeEmbeddingRepository repository) {
        this.gitHubClientService = gitHubClientService;
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    public void startIngestion(String owner, String repo) {
        CompletableFuture.runAsync(() -> {
            log.info("Starting background ingestion for {}/{}", owner, repo);
            
            gitHubClientService.getRepositoryContents(owner, repo, "")
                .blockOptional()
                .ifPresent(contents -> {
                    contents.stream()
                        .filter(c -> "file".equals(c.type()) && (c.name().endsWith(".md") || c.name().endsWith(".txt"))) 
                        .forEach(file -> processFile(owner, repo, file.path(), file.downloadUrl()));
                });
                
            log.info("Ingestion completed for {}/{}", owner, repo);
        });
    }

    private void processFile(String owner, String repo, String path, String downloadUrl) {
        try {
            String rawText = gitHubClientService.fetchRawFileContent(downloadUrl).block();
            if (rawText == null || rawText.isBlank()) return;
            
            // Chunking strategy: split by markdown headers to keep contextual blocks together
            String[] chunks = rawText.split("(?=\\n## )"); 
            
            List<CodeEmbedding> entities = new ArrayList<>();
            for (String chunk : chunks) {
                if (chunk.trim().length() < 50) continue; // Skip chunks that are too small
                
                var pgVector = embeddingService.generateEmbedding(chunk);
                if (pgVector != null) {
                    CodeEmbedding entity = new CodeEmbedding();
                    entity.setRepoName(owner + "/" + repo);
                    entity.setFilePath(path);
                    entity.setRawContent(chunk);
                    entity.setEmbedding(pgVector);
                    entities.add(entity);
                }
            }
            
            repository.saveAll(entities);
            log.info("Saved {} vector chunks for file: {}", entities.size(), path);
            
        } catch (Exception e) {
            log.error("Failed to process file {}: {}", path, e.getMessage());
        }
    }
}
