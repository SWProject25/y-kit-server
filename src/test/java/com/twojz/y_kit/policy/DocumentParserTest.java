package com.twojz.y_kit.policy;

import com.twojz.y_kit.policy.domain.vo.DocumentParsed;
import com.twojz.y_kit.policy.service.DocumentPreprocessor;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentParserTest {

    @Test
    @DisplayName("1. 명확한 쉼표 구분 리스트 테스트 (괄호, 공백 포함)")
    void testCommaSeparatedList() {
        String originalText = "주민등록초본, 혼인관계증명서(상세), 최종학력 증명서, 건강보험자격득실 확인서, 건강보험료 납부 확인서";

        DocumentParsed result = DocumentPreprocessor.parse(originalText);

        assertEquals("필수", result.getCondition());
        List<String> expected = Arrays.asList(
                "주민등록초본",
                "혼인관계증명서",
                "최종학력 증명서",
                "건강보험자격득실 확인서",
                "건강보험료 납부 확인서"
        );
        assertEquals(expected, result.getRequired_documents());
    }

    @Test
    @DisplayName("2. 비정형 목록 기호 및 줄바꿈 테스트")
    void testUnstructuredList() {
        String originalText =
                "- 홈페이지 신청화면에서 작성\n" +
                        "① 청년 자격증 응시료 지원 신청서\n" +
                        "② 개인정보 수집·이용·제공 동의 안내문\n" +
                        "○ 증빙자료 업로드(6종 필수. 주민번호 뒷자리 미표기)\n" +
                        "• 통장사본 : 신청자 본인 명의";

        DocumentParsed result = DocumentPreprocessor.parse(originalText);

        // condition이 명시되지 않았으므로 '필수'로 설정되어야 함
        assertEquals("필수", result.getCondition());
        List<String> expected = Arrays.asList(
                "홈페이지 신청화면에서 작성",
                "청년 자격증 응시료 지원 신청서",
                "개인정보 수집·이용·제공 동의 안내문",
                "증빙자료 업로드",
                "통장사본"
        );
        assertEquals(expected, result.getRequired_documents());
    }

    @Test
    @DisplayName("3. 조건(필수/선택) 추출 테스트")
    void testConditionExtraction() {
        String requiredText = "[필수] ① 신청서(서식1) ② 개인정보 동의서";
        String optionalText = "[선택] ⑨ 가족관계증명서 ⑩ 근로계약서";
        String mixedText = "[필수] A 서류, [선택] B 서류";

        // 필수 테스트
        DocumentParsed requiredResult = DocumentPreprocessor.parse(requiredText);
        assertEquals("필수", requiredResult.getCondition());

        // 선택 테스트
        DocumentParsed optionalResult = DocumentPreprocessor.parse(optionalText);
        assertEquals("선택", optionalResult.getCondition());

        // 혼합 테스트 (로직에 따라 '선택'이 최종 반영되어야 함)
        DocumentParsed mixedResult = DocumentPreprocessor.parse(mixedText);
        assertEquals("선택", mixedResult.getCondition());
        List<String> expectedMixed = Arrays.asList("A 서류", "B 서류");
        assertEquals(expectedMixed, mixedResult.getRequired_documents());
    }

    @Test
    @DisplayName("4. 복잡한 기호와 콜론 상세 설명 제거 테스트")
    void testComplexCleaning() {
        String originalText = "① 건강보험자격득실확인서: 필수 제출, ⑥ 사실증명(사업자등록사실여부) : 추가 자료 요청 가능";

        DocumentParsed result = DocumentPreprocessor.parse(originalText);

        assertEquals("필수", result.getCondition());
        List<String> expected = Arrays.asList(
                "건강보험자격득실확인서",
                "사실증명"
        );
        assertEquals(expected, result.getRequired_documents());
    }

    @Test
    @DisplayName("5. 빈 문자열 또는 null 입력 테스트")
    void testEmptyOrNullInput() {
        // Null 테스트
        DocumentParsed nullResult = DocumentPreprocessor.parse(null);
        assertEquals("필수", nullResult.getCondition());
        assertTrue(nullResult.getRequired_documents().isEmpty());

        // 빈 문자열 테스트
        DocumentParsed emptyResult = DocumentPreprocessor.parse("");
        assertEquals("필수", emptyResult.getCondition());
        assertTrue(emptyResult.getRequired_documents().isEmpty());
    }
}