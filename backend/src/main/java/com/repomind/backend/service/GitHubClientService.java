package com.repomind.backend.service;

import com.repomind.backend.dto.GitHubContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GitHubClientService {

    private final WebClient webClient;
    
    @Value("${github.api.token:}")
    private String githubApiToken;

    public GitHubClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    public Mono<List<GitHubContentResponse>> getRepositoryContents(String owner, String repo, String path) {
        String uri = String.format("/repos/%s/%s/contents/%s", owner, repo, path != null ? path : "");
        
        return webClient.get()
                .uri(uri)
                .headers(headers -> {
                    headers.set("Accept", "application/vnd.github.v3+json");
                    headers.set("User-Agent", "RepoMind-App");
                    if (githubApiToken != null && !githubApiToken.isEmpty()) {
                        headers.setBearerAuth(githubApiToken);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GitHubContentResponse>>() {});
    }
    
    public Mono<String> fetchRawFileContent(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return Mono.empty();
        }
        return WebClient.create().get()
                .uri(downloadUrl)
                .retrieve()
                .bodyToMono(String.class);
    }
}
