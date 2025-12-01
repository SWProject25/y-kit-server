package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.vo.DocumentParsed;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DocumentPreprocessor {
    private static final Pattern CLEANING_PATTERNS = Pattern.compile(
            "\\s*\\([^)]*\\)|" +        // 괄호와 그 내용 제거 (미리 처리)
                    "\\s*:\\s*(.*?)(?=\\s*,|\\s*$)|" + // 콜론 뒤의 상세 설명 제거
                    "[\\d\\.\\-\\+*#□❍○◦●※ㅇ■•①-⑳()<> ]"// 숫자, 기호, 특수문자 제거
    );

    public static DocumentParsed parse(String originalText) {
        if (originalText == null || originalText.isBlank()) {
            return DocumentParsed.builder()
                    .condition("필수")
                    .required_documents(new ArrayList<>())
                    .build();
        }

        String condition = "필수";
        String tempText = originalText;

        if (tempText.contains("[선택]")) {
            condition = "선택";
        }
        tempText = tempText.replaceAll("\\[필수\\]|\\[선택\\]", "");

        tempText = tempText.replaceAll("[\r\n\t]+", ", ");

        tempText = tempText.replaceAll("(?<=\\w)\\s*[-\\○●※❍□■◦ㅇ❍•①-⑳]", ", ");


        String cleanedText = CLEANING_PATTERNS.matcher(tempText).replaceAll(" ").trim();

        cleanedText = cleanedText.replaceAll("\\s+", " ").trim();

        String[] commaParts = cleanedText.split(",");
        List<String> requiredDocuments = new ArrayList<>();

        for (String part : commaParts) {
            String documentName = part.trim();

            if (!documentName.isEmpty()) {
                requiredDocuments.add(documentName);
            }
        }

        return DocumentParsed.builder()
                .condition(condition)
                .required_documents(requiredDocuments)
                .build();
    }
}