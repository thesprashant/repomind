package com.repomind.backend.service;

import com.pgvector.PGvector;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public PGvector generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Call OpenAI API to generate a 1536-dimensional embedding
        List<Double> embeddingList = embeddingModel.embed(text);
        
        // Convert List<Double> to float[] for the PGvector wrapper
        float[] floatArray = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            floatArray[i] = embeddingList.get(i).floatValue();
        }
        
        return new PGvector(floatArray);
    }
}
