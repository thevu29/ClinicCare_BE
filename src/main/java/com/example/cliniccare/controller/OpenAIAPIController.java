package com.example.cliniccare.controller;

import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.OpenAIAPIService;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/ai")
public class OpenAIAPIController {
    private final OpenAIAPIService openAIAPIService;
    private static final Logger logger = LoggerFactory.getLogger(OpenAIAPIController.class);

    @Autowired
    public OpenAIAPIController(OpenAIAPIService openAIAPIService) {
        this.openAIAPIService = openAIAPIService;
    }

    @GetMapping("/chat")
    public ResponseEntity<?> chat(@RequestParam String prompt) {
        try {
            String response = openAIAPIService.chat(prompt);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get chat response successfully", response
            ));
        } catch (Exception e) {
            logger.error("Failed to get chat response: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to get chat response", null
            ));
        }
    }
}
