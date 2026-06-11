package com.repomind.backend.service;

import com.repomind.backend.dto.GitHubContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

@Service
public class GitHubClientService {

    private final WebClient webClient;
    
    @Value("${github.api.token:}")
    private String githubApiToken;

    public GitHubClientService(WebClient.Builder webClientBuilder, 
                               @Value("${github.api.url:https://api.github.com}") String githubApiUrl) {
        HttpClient httpClient = HttpClient.create().followRedirect(true);
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(githubApiUrl)
                .build();
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
                .bodyToMono(new ParameterizedTypeReference<List<GitHubContentResponse>>() {})
                .retryWhen(getRetrySpec());
    }
    
    public Mono<String> fetchRawFileContent(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return Mono.empty();
        }
        return WebClient.create().get()
                .uri(downloadUrl)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(getRetrySpec());
    }

    private Retry getRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(2))
                .filter(throwable -> throwable instanceof WebClientResponseException ex
                    && (ex.getStatusCode().value() == 403 || ex.getStatusCode().value() == 429))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                    new RuntimeException("GitHub API rate limit exhausted after " + retrySignal.totalRetries() + " retries"));
    }
}
