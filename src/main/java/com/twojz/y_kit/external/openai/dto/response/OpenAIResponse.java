package com.twojz.y_kit.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class OpenAIResponse {
    private String id;
    private String object;
    @JsonProperty("created_at")
    private long created_at;
    private String status;
    private String model;
    private List<Output> output;
    private Usage usage;

    @Getter
    @Builder
    public static class Output {
        private String type;
        private String id;
        private String status;
        private String role;
        private List<Content> content;
    }

    @Getter
    @Builder
    public static class Content {
        private String type;
        private String text;
    }

    @Getter
    @Builder
    public static class Usage {
        @JsonProperty("input_tokens")
        private int input_tokens;
        @JsonProperty("output_tokens")
        private int output_tokens;
        @JsonProperty("total_tokens")
        private int total_tokens;
    }

    public String getFirstMessageContent() {
        if (output == null) return null;

        return output.stream()
                .filter(o -> "message".equals(o.getType()))
                .map(Output::getContent)
                .filter(content -> content != null && !content.isEmpty())
                .findFirst()
                .flatMap(contentList -> contentList.stream().findFirst())
                .map(Content::getText)
                .orElse(null);
    }
}