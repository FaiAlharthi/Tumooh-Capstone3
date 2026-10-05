package org.fadhel.tumoohplatform.dto.ai;


import java.util.List;

public record GeminiResponseDto(List<Candidate> candidates) {
    public record Candidate(Content content) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    public String getTextResponse() {
        if (candidates != null && !candidates.isEmpty()) {
            List<Part> parts = candidates.get(0).content().parts();
            if (parts != null && !parts.isEmpty()) {
                return parts.get(0).text();
            }
        }
        return "";
    }
}
