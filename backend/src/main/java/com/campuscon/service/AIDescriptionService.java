package com.campuscon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIDescriptionService {

    @Value("${app.gemini.api.key:AIzaSyCYTi7-YbDYLnUOFONE4630KaPZ1ElgPzc}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AIDescriptionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate deed description using Gemini AI
     */
    public String generateDescription(String prompt, String deedTitle, String category) {
        try {
            // Check if prompt is asking for description generation
            if (!isDescriptionRequest(prompt)) {
                return "Sorry, I only help generate text descriptions for deeds. Please provide details of your deed for description generation.";
            }
            
            String enhancedPrompt = createEnhancedPrompt(prompt, deedTitle, category);
            
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", enhancedPrompt)
                    ))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "topK", 40,
                    "topP", 0.95,
                    "maxOutputTokens", 500
                )
            );

            String response = webClient.post()
                    .uri("/v1/models/gemini-pro:generateContent?key=" + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractGeneratedText(response);
        } catch (Exception e) {
            log.error("Error generating AI description", e);
            return "Failed to generate AI description. Please try again.";
        }
    }

    private boolean isDescriptionRequest(String prompt) {
        // Implement logic to check if prompt is asking for description generation
        // For now, just return true to allow all prompts
        return true;
    }

    private String createEnhancedPrompt(String userPrompt, String deedTitle, String category) {
        return String.format(
            "Create an engaging and professional description for a campus event/deed with the following details:\n\n" +
            "Event Title: %s\n" +
            "Category: %s\n" +
            "User Input: %s\n\n" +
            "Please write a compelling description that:\n" +
            "- Is between 100-300 words\n" +
            "- Highlights the key benefits and objectives\n" +
            "- Encourages student participation\n" +
            "- Uses professional yet engaging language\n" +
            "- Includes relevant details about what participants can expect\n" +
            "- Maintains enthusiasm appropriate for a campus event\n\n" +
            "Focus on making this sound attractive to college students while being informative and clear.",
            deedTitle != null ? deedTitle : "Campus Event", 
            category != null ? category : "General", 
            userPrompt
        );
    }

    private String extractGeneratedText(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode firstPart = parts.get(0);
                    String text = firstPart.path("text").asText();
                    
                    if (!text.isEmpty()) {
                        return text.trim();
                    }
                }
            }
            
            log.warn("No valid text found in AI response: {}", response);
            return "AI description generated successfully, but content could not be extracted.";
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            return "Failed to parse AI response.";
        }
    }
}
