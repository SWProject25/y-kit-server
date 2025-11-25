package com.twojz.y_kit.external.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmMessage {
    private String title;
    private String body;
    private String image;
    private Map<String, String> data;
}