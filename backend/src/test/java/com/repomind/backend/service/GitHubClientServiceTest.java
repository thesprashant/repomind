package com.repomind.backend.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class GitHubClientServiceTest {

    private MockWebServer mockWebServer;
    private GitHubClientService gitHubClientService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        WebClient.Builder builder = WebClient.builder();
        String baseUrl = mockWebServer.url("/").toString();
        
        gitHubClientService = new GitHubClientService(builder, baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getRepositoryContents_Success() {
        String mockJsonResponse = "[{\"name\":\"README.md\",\"path\":\"README.md\",\"type\":\"file\",\"size\":1024,\"download_url\":\"http://localhost/README.md\"}]";
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockJsonResponse)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(gitHubClientService.getRepositoryContents("facebook", "react", ""))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).name().equals("README.md"))
                .verifyComplete();
    }

    @Test
    void getRepositoryContents_RateLimitRetryThenThrow() {
        // Enqueue 4 rate limit responses (3 retries + initial)
        for (int i = 0; i < 4; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(403)
                    .setBody("{\"message\": \"API rate limit exceeded\"}")
                    .addHeader("Content-Type", "application/json"));
        }

        StepVerifier.create(gitHubClientService.getRepositoryContents("facebook", "react", ""))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("rate limit exhausted"))
                .verify();
    }
    
    @Test
    void fetchRawFileContent_Success() {
        String mockContent = "# Hello World";
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockContent)
                .addHeader("Content-Type", "text/plain"));

        String mockUrl = mockWebServer.url("/README.md").toString();

        StepVerifier.create(gitHubClientService.fetchRawFileContent(mockUrl))
                .expectNext("# Hello World")
                .verifyComplete();
    }
}
