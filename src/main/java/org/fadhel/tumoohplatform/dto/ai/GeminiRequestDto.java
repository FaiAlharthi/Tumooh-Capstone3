package org.fadhel.tumoohplatform.dto.ai;


import java.util.List;

public class GeminiRequestDto {

    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    private final List<Content> contents;

    public GeminiRequestDto(String prompt) {
        this.contents = List.of(new Content(List.of(new Part(prompt))));
    }

    public List<Content> getContents() {
        return contents;
    }
}
