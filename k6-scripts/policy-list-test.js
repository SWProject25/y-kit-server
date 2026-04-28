import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

const listDuration    = new Trend('policy_list_duration', true);
const searchDuration  = new Trend('policy_search_duration', true);
const popularDuration = new Trend('policy_popular_duration', true);
const errorRate       = new Rate('policy_error_rate');
const emptyListCount  = new Counter('policy_empty_results');

export const options = {
    stages: [
        { duration: '30s', target: 10 },  // 워밍업
        { duration: '1m',  target: 50 },  // 점진적 증가
        { duration: '2m',  target: 50 },  // 부하 유지 (병목 관찰 구간)
        { duration: '30s', target: 100 }, // 스파이크
        { duration: '30s', target: 0 },   // 종료
    ],
    thresholds: {
        'http_req_duration':                          ['p(95)<2000'],
        'http_req_duration{name:policy_list}':        ['p(95)<2000'],
        'http_req_duration{name:policy_search}':      ['p(95)<3000'],
        'http_req_duration{name:policy_popular}':     ['p(95)<2000'],
        'policy_error_rate':                          ['rate<0.05'],
    },
};

const BASE_URL = 'http://host.docker.internal:8080';

const KEYWORDS = [
    '청년',
    '주거',
    '취업',
    '창업',
    '대출',
    '월세',
    '자격증',
    '교통비',
    '문화',
    '복지',
];

const CATEGORY_IDS = [1, 2, 3, 4, 5];

const SORT_OPTIONS = [
    'createdAt,desc',
    'viewCount,desc',
    'bookmarkCount,desc',
];

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export default function () {
    const page = Math.floor(Math.random() * 5);
    const size = 10;

    // 시나리오 A: 정책 목록 조회 (기본 — 가장 빈번한 접근)
    group('정책 목록 조회', () => {
        const sort = pick(SORT_OPTIONS);
        const url  = `${BASE_URL}/api/v1/policies?page=${page}&size=${size}&sort=${sort}`;

        const res = http.get(url, { tags: { name: 'policy_list' } });

        listDuration.add(res.timings.duration);
        errorRate.add(res.status !== 200);

        const ok = check(res, {
            'status 200':           (r) => r.status === 200,
            'has content':          (r) => Array.isArray(r.json('content')),
            'has totalElements':    (r) => r.json('totalElements') !== undefined,
            'has totalPages':       (r) => r.json('totalPages') !== undefined,
            'response < 2s':        (r) => r.timings.duration < 2000,
        });

        if (!ok) {
            console.error(`[policy_list] sort="${sort}" status=${res.status} duration=${res.timings.duration}ms`);
        }

        try {
            const body = res.json();
            if (body && body.totalElements === 0) {
                emptyListCount.add(1);
            }
        } catch (_) {}
    });

    sleep(Math.random() * 1.5 + 0.5);

    // 시나리오 B: 키워드 검색 (형태소 분석 포함)
    group('정책 키워드 검색', () => {
        const keyword = pick(KEYWORDS);
        const url     = `${BASE_URL}/api/v1/policies/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}&sort=createdAt,desc`;

        const res = http.get(url, { tags: { name: 'policy_search' } });

        searchDuration.add(res.timings.duration);
        errorRate.add(res.status !== 200);

        const ok = check(res, {
            'status 200':           (r) => r.status === 200,
            'has content':          (r) => Array.isArray(r.json('content')),
            'has totalElements':    (r) => r.json('totalElements') !== undefined,
            'response < 3s':        (r) => r.timings.duration < 3000,
        });

        if (!ok) {
            console.error(`[policy_search] keyword="${keyword}" status=${res.status} duration=${res.timings.duration}ms`);
        }
    });

    sleep(Math.random() * 1 + 0.3);

    // 시나리오 C: 카테고리 필터 검색 (약 50% 확률 — 선택적 행동 시뮬레이션)
    if (Math.random() < 0.5) {
        group('카테고리 필터 검색', () => {
            const categoryId = pick(CATEGORY_IDS);
            const url        = `${BASE_URL}/api/v1/policies/search?categoryIds=${categoryId}&page=0&size=${size}&sort=createdAt,desc`;

            const res = http.get(url, { tags: { name: 'policy_search' } });

            searchDuration.add(res.timings.duration);
            errorRate.add(res.status !== 200);

            check(res, {
                'status 200':         (r) => r.status === 200,
                'has content':        (r) => Array.isArray(r.json('content')),
                'has totalElements':  (r) => r.json('totalElements') !== undefined,
                'response < 3s':      (r) => r.timings.duration < 3000,
            });

            if (res.status !== 200) {
                console.error(`[policy_category] categoryId=${categoryId} status=${res.status}`);
            }
        });

        sleep(Math.random() * 0.5 + 0.2);
    }

    // 시나리오 D: 인기 정책 조회 (약 30% 확률 — 홈 화면 진입 시뮬레이션)
    if (Math.random() < 0.3) {
        group('인기 정책 조회', () => {
            const sortBy = Math.random() < 0.5 ? 'viewCount' : 'bookmarkCount';
            const url    = `${BASE_URL}/api/v1/policies/popular?sortBy=${sortBy}&page=0&size=${size}`;

            const res = http.get(url, { tags: { name: 'policy_popular' } });

            popularDuration.add(res.timings.duration);
            errorRate.add(res.status !== 200);

            check(res, {
                'status 200':        (r) => r.status === 200,
                'has content':       (r) => Array.isArray(r.json('content')),
                'has totalElements': (r) => r.json('totalElements') !== undefined,
                'response < 2s':     (r) => r.timings.duration < 2000,
            });

            if (res.status !== 200) {
                console.error(`[policy_popular] sortBy=${sortBy} status=${res.status}`);
            }
        });

        sleep(Math.random() * 0.5 + 0.2);
    }
}

// ─── 테스트 종료 후 요약 출력
export function handleSummary(data) {
    const list    = data.metrics['http_req_duration{name:policy_list}'];
    const search  = data.metrics['http_req_duration{name:policy_search}'];
    const popular = data.metrics['http_req_duration{name:policy_popular}'];
    const errors  = data.metrics['policy_error_rate'];
    const empty   = data.metrics['policy_empty_results'];

    const fmt = (v) => (v !== undefined ? v.toFixed(0) + 'ms' : 'N/A');

    const summary = `
=== 정책 목록조회 부하테스트 결과 ===

[정책 목록 조회 - policy_list]
  avg   : ${fmt(list?.values?.avg)}
  p(90) : ${fmt(list?.values?.['p(90)'])}
  p(95) : ${fmt(list?.values?.['p(95)'])}
  p(99) : ${fmt(list?.values?.['p(99)'])}
  max   : ${fmt(list?.values?.max)}

[정책 검색 (키워드/카테고리) - policy_search]
  avg   : ${fmt(search?.values?.avg)}
  p(90) : ${fmt(search?.values?.['p(90)'])}
  p(95) : ${fmt(search?.values?.['p(95)'])}
  p(99) : ${fmt(search?.values?.['p(99)'])}
  max   : ${fmt(search?.values?.max)}

[인기 정책 조회 - policy_popular]
  avg   : ${fmt(popular?.values?.avg)}
  p(95) : ${fmt(popular?.values?.['p(95)'])}

[에러율]
  rate  : ${((errors?.values?.rate ?? 0) * 100).toFixed(2)}%

[빈 결과 수]
  count : ${empty?.values?.count ?? 0}건

※ 개선 전 수치를 기록해두고 최적화 적용 후 비교하세요.
`;

    console.log(summary);

    return { stdout: summary };
}
