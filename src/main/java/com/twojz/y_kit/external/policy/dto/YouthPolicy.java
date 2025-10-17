package com.twojz.y_kit.external.policy.dto;

import lombok.Data;

@Data
public class YouthPolicy {
    // 기본 정보
    private String plcyNo;              // 정책번호
    private String plcyNm;              // 정책명
    private String plcyKywdNm;          // 정책키워드명
    private String plcyExplnCn;         // 정책설명내용
    private String plcySprtCn;          // 정책지원내용

    // 분류
    private String lclsfNm;             // 정책대분류명
    private String mclsfNm;             // 정책중분류명

    // 기본계획 정보
    private String bscPlanCycl;         // 기본계획차수
    private String bscPlanPlcyWayNo;    // 기본계획정책방향번호
    private String bscPlanFcsAsmtNo;    // 기본계획중점과제번호
    private String bscPlanAsmtNo;       // 기본계획과제번호

    // 코드 정보
    private String pvsnInstGroupCd;     // 제공기관그룹코드
    private String plcyPvsnMthdCd;      // 정책제공방법코드
    private String plcyAprvSttsCd;      // 정책승인상태코드

    // 주관/운영 기관
    private String sprvsnInstCd;        // 주관기관코드
    private String sprvsnInstCdNm;      // 주관기관코드명
    private String sprvsnInstPicNm;     // 주관기관담당자명
    private String operInstCd;          // 운영기관코드
    private String operInstCdNm;        // 운영기관코드명
    private String operInstPicNm;       // 운영기관담당자명

    // 지원 규모
    private String sprtSclLmtYn;        // 지원규모제한여부
    private String sprtSclCnt;          // 지원규모수
    private String sprtArvlSeqYn;       // 지원도착순서여부

    // 기간 정보
    private String aplyPrdSeCd;         // 신청기간구분코드
    private String bizPrdSeCd;          // 사업기간구분코드
    private String bizPrdBgngYmd;       // 사업기간시작일자
    private String bizPrdEndYmd;        // 사업기간종료일자
    private String bizPrdEtcCn;         // 사업기간기타내용
    private String aplyYmd;             // 신청기간

    // 신청 방법
    private String plcyAplyMthdCn;      // 정책신청방법내용
    private String srngMthdCn;          // 심사방법내용
    private String aplyUrlAddr;         // 신청URL주소
    private String sbmsnDcmntCn;        // 제출서류내용
    private String etcMttrCn;           // 기타사항내용
    private String refUrlAddr1;         // 참고URL주소1
    private String refUrlAddr2;         // 참고URL주소2

    // 지원 대상 - 연령
    private String sprtTrgtMinAge;      // 지원대상최소연령
    private String sprtTrgtMaxAge;      // 지원대상최대연령
    private String sprtTrgtAgeLmtYn;    // 지원대상연령제한여부

    // 지원 대상 - 결혼/소득
    private String mrgSttsCd;           // 결혼상태코드
    private String earnCndSeCd;         // 소득조건구분코드
    private String earnMinAmt;          // 소득최소금액
    private String earnMaxAmt;          // 소득최대금액
    private String earnEtcCn;           // 소득기타내용

    // 추가 자격 조건
    private String addAplyQlfcCndCn;    // 추가신청자격조건내용
    private String ptcpPrpTrgtCn;       // 참여제안대상내용

    // 지역/전공/취업/학력 코드
    private String zipCd;               // 정책거주지역코드
    private String plcyMajorCd;         // 정책전공요건코드
    private String jobCd;               // 정책취업요건코드
    private String schoolCd;            // 정책학력요건코드
    private String sBizCd;              // 정책특화요건코드

    // 등록 정보
    private String rgtrInstCd;          // 등록자기관코드
    private String rgtrInstCdNm;        // 등록자기관코드명
    private String rgtrUpInstCd;        // 등록자상위기관코드
    private String rgtrUpInstCdNm;      // 등록자상위기관코드명
    private String rgtrHghrkInstCd;     // 등록자최상위기관코드
    private String rgtrHghrkInstCdNm;   // 등록자최상위기관코드명

    // 기타
    private String inqCnt;              // 조회수
    private String frstRegDt;           // 최초등록일시
    private String lastMdfcnDt;         // 최종수정일시
}
