package com.twojz.y_kit.external.openai.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class OpenAIResponse {
    private String id;
    private String object;
    private long created_at;
    private String status;
    private String model;
    private List<Output> output;
    private Usage usage;

    @Data
    public static class Output {
        private String type;
        private String id;
        private String status;
        private String role;
        private List<Content> content;
    }

    @Data
    public static class Content {
        private String type;
        private String text;
    }

    @Data
    public static class Usage {
        private int input_tokens;
        private int output_tokens;
        private int total_tokens;
    }

    public String getFirstMessageContent() {
        if (output == null) return null;

        return output.stream()
                .filter(o -> "message".equals(o.getType()))
                .filter(o -> o.getContent() != null && !o.getContent().isEmpty())
                .findFirst()
                .map(o -> o.getContent().getFirst().getText())
                .orElse(null);
    }
}