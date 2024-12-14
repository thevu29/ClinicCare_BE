package com.example.cliniccare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class OpenAIAPIService {
    private static final String BASE_URL = "https://api.pawan.krd/cosmosrp/v1/";
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Autowired
    public OpenAIAPIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    public String chat(String prompt) {
        String url = BASE_URL + "chat/completions";

        Map<String, Object> payload = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt),
                        Map.of("role", "user", "content", prompt)
                }
        );

        String response = webClientBuilder.build()
                .post()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractContent(response);
    }

    private String extractContent(String jsonString) {
        try {
            JsonNode root = objectMapper.readTree(jsonString);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            return content.replaceAll("\\*.*?\\*", "").trim();
        } catch (Exception e) {
            return "Error extracting content";
        }
    }
}
