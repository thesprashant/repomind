package com.repomind.backend.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Call the local ONNX transformer model to generate a 384-dimensional embedding
        return embeddingModel.embed(text);
    }
}
