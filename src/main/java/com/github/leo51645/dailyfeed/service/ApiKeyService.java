package com.github.leo51645.dailyfeed.service;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final GeminiService geminiService;
    private final CurrentsNewsService currentsNewsService;

    private void writeApiKeys(String geminiApiKey, String currentsApiKey) {
        try {
            Files.writeString(Path.of(".env"),"GEMINI_API_KEY=" + geminiApiKey + "\nCURRENTS_API_KEY=" + currentsApiKey);
        } catch (IOException e) {
            throw new RuntimeException(e); //Todo: Exception handling
        }
    }

    public boolean validateApiKeyGemini(String apiKeyGemini) {
        boolean geminiApiKeyIsValid = true;

        try {
            geminiService.testApiKey(apiKeyGemini);
        } catch (Exception e) {
            geminiApiKeyIsValid = false;
        }
        return geminiApiKeyIsValid;
    }
    public boolean validateApiKeyCurrents(String apiKeyCurrents) {
        boolean currentsApiKeyIsValid = true;

        try {
            HttpResponse<String> response = currentsNewsService.testApiKey(apiKeyCurrents);
            if (response.statusCode() != 200) {
                currentsApiKeyIsValid = false;
            }
        } catch (Exception e) {
            currentsApiKeyIsValid = false;
        }
        return currentsApiKeyIsValid;
    }

}
