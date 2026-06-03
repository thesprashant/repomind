package com.repomind.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GitHubClientServiceManualTest {

    @Test
    public void testFetchFromGitHub() {
        // Instantiate the service manually without booting the whole Spring Context
        GitHubClientService service = new GitHubClientService(WebClient.builder());
        
        System.out.println("Fetching repository contents for 'spring-projects/spring-boot'...");
        
        // .block() forces the reactive Mono to execute synchronously for the test
        var contents = service.getRepositoryContents("spring-projects", "spring-boot", "").block();
        
        assertNotNull(contents, "Response should not be null");
        assertFalse(contents.isEmpty(), "Response should contain files");
        
        System.out.println("✅ Successfully fetched " + contents.size() + " items from GitHub!");
        
        // Print the first 3 items to prove it works
        contents.stream().limit(3).forEach(item -> 
            System.out.println(" -> " + item.name() + " | Type: " + item.type())
        );
    }
}
