import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

const searchDuration   = new Trend('search_duration', true);   // 전체 응답 시간
const searchErrorRate  = new Rate('search_error_rate');        // 에러율
const emptyResultCount = new Counter('search_empty_results');  // 결과 0건 카운트

export const options = {
    stages: [
        { duration: '30s', target: 10 },  // 워밍업
        { duration: '1m',  target: 50 },  // 점진적 증가
        { duration: '2m',  target: 50 },  // 부하 유지 (병목 관찰 구간)
        { duration: '30s', target: 100 }, // 스파이크
        { duration: '30s', target: 0 },   // 종료
    ],
    thresholds: {
        // 개선 전 현실적인 기준 — 테스트 후 수치 기록용
        'http_req_duration':                      ['p(95)<3000'],
        'http_req_duration{name:search_all}':     ['p(95)<3000'],
        'http_req_duration{name:search_preview}': ['p(95)<2000'],
        'search_error_rate':                      ['rate<0.05'],
    },
};

const BASE_URL = 'http://host.docker.internal:8080';

const KEYWORDS = [
    '청년',
    '청년 주거',
    '취업 지원',
    '창업',
    '대출',
    '월세',
    '자격증',
    '청년내일저축',
    '교통비',
    '문화',
];

function randomKeyword() {
    return KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
}

// 메인 시나리오
export default function () {
    const keyword = randomKeyword();
    const page    = Math.floor(Math.random() * 3);
    const size    = 10;

    // 시나리오 A: 전체 통합 검색 (핵심 — 4개 도메인 순차 실행)
    group('전체 통합 검색', () => {
        const url = `${BASE_URL}/api/v1/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}&sort=createdAt,desc`;

        const res = http.get(url, {
            tags: { name: 'search_all' },
        });

        searchDuration.add(res.timings.duration);
        searchErrorRate.add(res.status !== 200);

        const ok = check(res, {
            'status 200':             (r) => r.status === 200,
            'has keyword field':      (r) => r.json('keyword') !== undefined,
            'has policies':           (r) => r.json('policies') !== null,
            'has communities':        (r) => r.json('communities') !== null,
            'has hotDeals':           (r) => r.json('hotDeals') !== null,
            'has groupPurchases':     (r) => r.json('groupPurchases') !== null,
            'response < 3s':         (r) => r.timings.duration < 3000,
        });

        if (!ok) {
            console.error(`[search_all] keyword="${keyword}" status=${res.status} duration=${res.timings.duration}ms`);
        }

        // 결과 0건 여부 추적 (NLP 형태소 분석 실패 지표)
        try {
            const body = res.json();
            if (body && body.totalCount === 0) {
                emptyResultCount.add(1);
            }
        } catch (_) {}
    });

    sleep(Math.random() * 1.5 + 0.5); // 0.5~2s 사용자 행동 간격

    // 시나리오 B: 미리보기 검색 (앱 검색창 자동완성 대응)
    // GlobalSearchService.searchPreview() — 각 도메인 5개씩, 동일한 순차 실행 구조
    group('미리보기 검색', () => {
        const previewKeyword = randomKeyword();
        const url = `${BASE_URL}/api/v1/search?keyword=${encodeURIComponent(previewKeyword)}&page=0&size=5&sort=createdAt,desc`;

        const res = http.get(url, {
            tags: { name: 'search_preview' },
        });

        check(res, {
            'preview status 200':   (r) => r.status === 200,
            'preview response < 2s': (r) => r.timings.duration < 2000,
        });

        if (res.status !== 200) {
            console.error(`[search_preview] keyword="${previewKeyword}" status=${res.status}`);
        }
    });

    sleep(Math.random() * 1 + 0.3);
}

// ─── 테스트 종료 후 요약 출력
export function handleSummary(data) {
    const searchAll     = data.metrics['http_req_duration{name:search_all}'];
    const searchPreview = data.metrics['http_req_duration{name:search_preview}'];
    const errorRate     = data.metrics['search_error_rate'];

    const fmt = (v) => (v !== undefined ? v.toFixed(0) + 'ms' : 'N/A');

    const summary = `
=== GlobalSearch 부하테스트 결과 ===

[전체 통합 검색 - search_all]
  avg   : ${fmt(searchAll?.values?.avg)}
  p(90) : ${fmt(searchAll?.values?.['p(90)'])}
  p(95) : ${fmt(searchAll?.values?.['p(95)'])}
  p(99) : ${fmt(searchAll?.values?.['p(99)'])}
  max   : ${fmt(searchAll?.values?.max)}

[미리보기 검색 - search_preview]
  avg   : ${fmt(searchPreview?.values?.avg)}
  p(95) : ${fmt(searchPreview?.values?.['p(95)'])}

[에러율]
  rate  : ${((errorRate?.values?.rate ?? 0) * 100).toFixed(2)}%

※ 개선 전 수치를 기록해두고, CompletableFuture 적용 후 비교하세요.
`;

    console.log(summary);

    return {
        stdout: summary,
    };
}
