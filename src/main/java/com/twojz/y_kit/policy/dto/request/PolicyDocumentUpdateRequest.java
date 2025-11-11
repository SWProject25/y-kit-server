package com.twojz.y_kit.policy.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolicyDocumentUpdateRequest {
    private String documentsOriginal;
    private boolean isRequired;
}
