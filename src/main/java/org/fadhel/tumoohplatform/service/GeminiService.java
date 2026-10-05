package org.fadhel.tumoohplatform.service;


import org.fadhel.tumoohplatform.dto.ai.GeminiRequestDto;
import org.fadhel.tumoohplatform.dto.ai.GeminiResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeminiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public GeminiService(
            RestClient restClient,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.base-url}") String baseUrl,
            @Value("${gemini.model}") String model) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public String generateText(String prompt) {
        String endpoint = String.format("%s/%s:generateContent?key=%s", baseUrl, model, apiKey);
        GeminiRequestDto request = new GeminiRequestDto(prompt);

        GeminiResponseDto response = restClient.post()
                .uri(endpoint)
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(GeminiResponseDto.class);

        return response != null ? response.getTextResponse() : "";
    }
}
