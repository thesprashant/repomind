package com.repomind.backend.dto;

public record JobStatus(
    String status, 
    int totalFiles, 
    int processedFiles
) {}
