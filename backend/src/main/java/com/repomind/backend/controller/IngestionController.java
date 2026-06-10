package com.repomind.backend.controller;

import com.repomind.backend.dto.IngestRequest;
import com.repomind.backend.dto.JobStatus;
import com.repomind.backend.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/repomind")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingestRepo(@RequestBody IngestRequest request) {
        String jobId = UUID.randomUUID().toString();
        
        // Kick off asynchronous ingestion process
        ingestionService.startIngestion(request.owner(), request.repo(), jobId);
        
        return ResponseEntity.accepted().body(Map.of(
                "status", "Ingestion started in the background",
                "jobId", jobId
        ));
    }
    
    @GetMapping("/ingest/status/{jobId}")
    public ResponseEntity<JobStatus> getJobStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(ingestionService.getJobStatus(jobId));
    }
}
