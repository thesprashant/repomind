package com.repomind.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.transformers.TransformersEmbeddingModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class EmbeddingServiceTest {

    private TransformersEmbeddingModel mockEmbeddingModel;
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        mockEmbeddingModel = Mockito.mock(TransformersEmbeddingModel.class);
        embeddingService = new EmbeddingService(mockEmbeddingModel);
    }

    @Test
    void generateEmbedding_Success() {
        float[] expectedEmbedding = {0.1f, 0.2f, 0.3f};
        Mockito.when(mockEmbeddingModel.embed(anyString())).thenReturn(expectedEmbedding);

        float[] result = embeddingService.generateEmbedding("Hello World");

        assertNotNull(result);
        assertArrayEquals(expectedEmbedding, result);
        Mockito.verify(mockEmbeddingModel, Mockito.times(1)).embed("Hello World");
    }

    @Test
    void generateEmbedding_NullText() {
        float[] result = embeddingService.generateEmbedding(null);
        
        assertNull(result);
        Mockito.verify(mockEmbeddingModel, Mockito.never()).embed(anyString());
    }
    
    @Test
    void generateEmbedding_EmptyText() {
        float[] result = embeddingService.generateEmbedding("   ");
        
        assertNull(result);
        Mockito.verify(mockEmbeddingModel, Mockito.never()).embed(anyString());
    }
}
