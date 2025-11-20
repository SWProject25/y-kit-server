package com.twojz.y_kit.external.public_resource.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class PublicResourceResponse {
    private String resultCode;
    private String resultMsg;
    private List<ResourceData> data;
}