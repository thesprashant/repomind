package com.repomind.backend.service;

import com.repomind.backend.dto.JobStatus;
import com.repomind.backend.entity.CodeEmbedding;
import com.repomind.backend.repository.CodeEmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final GitHubClientService gitHubClientService;
    private final EmbeddingService embeddingService;
    private final CodeEmbeddingRepository repository;
    
    private final ConcurrentHashMap<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();

    public IngestionService(GitHubClientService gitHubClientService, 
                            EmbeddingService embeddingService, 
                            CodeEmbeddingRepository repository) {
        this.gitHubClientService = gitHubClientService;
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    public JobStatus getJobStatus(String jobId) {
        return jobStatuses.getOrDefault(jobId, new JobStatus("NOT_FOUND", 0, 0));
    }

    public void startIngestion(String owner, String repo, String jobId) {
        jobStatuses.put(jobId, new JobStatus("INITIALIZING", 0, 0));
        
        CompletableFuture.runAsync(() -> {
            log.info("Starting background ingestion for {}/{} with Job ID: {}", owner, repo, jobId);
            
            try {
                gitHubClientService.getRepositoryContents(owner, repo, "")
                    .blockOptional()
                    .ifPresent(contents -> {
                        var filesToProcess = contents.stream()
                            .filter(c -> "file".equals(c.type()) && (c.name().endsWith(".md") || c.name().endsWith(".txt")))
                            .toList();
                            
                        int total = filesToProcess.size();
                        jobStatuses.put(jobId, new JobStatus("IN_PROGRESS", total, 0));
                        
                        int processed = 0;
                        for (var file : filesToProcess) {
                            processFile(owner, repo, file.path(), file.downloadUrl());
                            processed++;
                            jobStatuses.put(jobId, new JobStatus("IN_PROGRESS", total, processed));
                        }
                    });
                    
                jobStatuses.put(jobId, new JobStatus("COMPLETED", jobStatuses.get(jobId).totalFiles(), jobStatuses.get(jobId).processedFiles()));
                log.info("Ingestion completed for {}/{}", owner, repo);
            } catch (Exception e) {
                log.error("Job {} failed: {}", jobId, e.getMessage());
                jobStatuses.put(jobId, new JobStatus("FAILED", 0, 0));
            }
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
