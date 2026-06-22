package com.github.leo51645.dailyfeed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final GeminiService geminiService;
    private final CurrentsNewsService currentsNewsService;

    private void writeApiKeys(String geminiApiKey, String currentsApiKey) {
        try {
            Files.writeString(Path.of(".env"), "GEMINI_API_KEY=" + geminiApiKey + "\nCURRENTS_API_KEY=" + currentsApiKey);
            log.info("API keys written to .env");
        } catch (IOException e) {
            log.error("Failed to write API keys to .env", e);
            throw new RuntimeException("Could not save API keys: " + e.getMessage(), e);
        }
    }

    public boolean validateApiKeyGemini(String apiKeyGemini) {
        log.info("Validating Gemini API key");
        try {
            geminiService.testApiKey(apiKeyGemini);
            log.info("Gemini API key is valid");
            return true;
        } catch (Exception e) {
            log.warn("Gemini API key validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateApiKeyCurrents(String apiKeyCurrents) {
        log.info("Validating Currents API key");
        try {
            HttpResponse<String> response = currentsNewsService.testApiKey(apiKeyCurrents);
            boolean valid = response.statusCode() == 200;
            if (valid) {
                log.info("Currents API key is valid");
            } else {
                log.warn("Currents API key validation failed with HTTP {}", response.statusCode());
            }
            return valid;
        } catch (Exception e) {
            log.warn("Currents API key validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean apiKeysAreConfigured() {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            log.debug("No .env file found");
            return false;
        }
        try {
            String envFile = Files.readString(envPath);
            boolean configured = envFile.contains("GEMINI_API_KEY=") && envFile.contains("CURRENTS_API_KEY=");
            log.debug("API keys configured: {}", configured);
            return configured;
        } catch (IOException e) {
            log.warn("Failed to read .env file while checking API key configuration", e);
            return false;
        }
    }

}
