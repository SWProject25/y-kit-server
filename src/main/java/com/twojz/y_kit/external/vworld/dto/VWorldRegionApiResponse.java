package com.twojz.y_kit.external.vworld.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VWorldRegionApiResponse {
    @JsonProperty("admVOList")
    private AdmVOList admVOList;

    @Getter
    public static class AdmVOList {
        private List<VWorldRegionItem> admVOList;
        private String totalCount;
        private String numOfRows;
        private String error;
        private String message;
    }

    @Getter
    public static class VWorldRegionItem {
        private String admCode;
        private String admCodeNm;
        private String lowestAdmCodeNm;
    }
}