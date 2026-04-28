# Agent Automation System Design

**Date:** 2026-04-28  
**Project:** autoSleep (Android / Kotlin + Jetpack Compose)

---

## Overview

Claude Code 슬래시 커맨드 기반의 AI 에이전트 자동화 시스템.  
`/planner`와 `/developer` 두 커맨드로 각각 기획자/개발자 페르소나를 활성화하여 브랜치 생성 → 작업 수행 → PR 생성까지 자동화한다.

---

## File Structure

```
autoSleep/
├── AGENTS.md                          # 공통 규칙 (브랜치, PR, 빌드 등)
├── docs/
│   ├── TODO.md                        # 요구사항 + 우선순위 목록
│   └── superpowers/specs/             # 설계 문서
└── .claude/
    └── commands/
        ├── planner.md                 # /planner 커맨드
        └── developer.md              # /developer 커맨드
```

---

## AGENTS.md — 공통 규칙

### 브랜치 네이밍
- planner: `feature/planning-YYYYMMDD`
- developer: `feature/<task-slug>` (TODO 태스크명을 kebab-case로 변환)

### PR 형식
- 제목: `[Planner] 요구사항 분석 및 TODO 업데이트` / `[Dev] <태스크명>`
- 본문: 변경 사항 요약 + `docs/TODO.md` 링크

### 빌드 검증 (developer 전용)
- `./gradlew assembleDebug` 성공 시에만 커밋 진행
- 빌드 실패 시 작업 중단 후 원인 보고

### TODO 우선순위 기준
| 레벨 | 기준 |
|-----|------|
| P0 | 크리티컬 버그 / 핵심 기능 누락 |
| P1 | 주요 기능 개선 |
| P2 | UX 개선 / 마이너 기능 |
| P3 | 리팩토링 / 기술 부채 |

---

## docs/TODO.md — 형식

```markdown
# AutoSleep TODO

## 진행 중
- [ ] (없음)

## 대기 중
| 우선순위 | 태스크 | 설명 | 추가일 |
|---------|-------|------|-------|
| P1 | 다크모드 지원 | 시스템 테마 연동 | 2026-04-28 |

## 완료
- [x] 위젯 추가 (2026-04-20)
```

---

## /planner 커맨드 동작

### 입력 없을 때 (`/planner`)
1. `main`에서 `feature/planning-YYYYMMDD` 브랜치 생성
2. 코드베이스 전체 스캔
   - 현재 구현된 기능 목록 파악 (screens, services, data sources)
   - 코드 내 `TODO` 주석 수집
   - 최근 커밋 히스토리 분석
3. 기획자 관점에서 추가 가능한 기능 도출
4. `docs/TODO.md` 업데이트
   - 신규 항목 추가 (우선순위 포함)
   - 기존 항목 우선순위 재정렬
5. 변경 내용 요약 커밋 → PR 생성

### 방향 입력 시 (`/planner "다크모드"`)
- 동일 흐름이지만 입력된 방향성에 집중해서 분석 및 요구사항 세분화

---

## /developer 커맨드 동작

1. `docs/TODO.md` **대기 중** 테이블에서 최우선순위 태스크 선택 (P0 → P1 → P2 → P3 순)
2. 선택한 태스크를 **진행 중** 섹션으로 이동 후 커밋
3. `feature/<task-slug>` 브랜치 생성
4. 태스크 구현
5. `./gradlew assembleDebug` 실행
   - 빌드 실패 시: 작업 중단, 원인 보고
   - 빌드 성공 시: 다음 단계 진행
6. 태스크를 **완료** 섹션으로 이동 + 구현 내용 커밋
7. PR 생성

---

## Data Flow

```
/planner
  └─> git checkout -b feature/planning-YYYYMMDD
  └─> 코드베이스 분석
  └─> docs/TODO.md 업데이트
  └─> git commit + push
  └─> gh pr create [Planner]

/developer
  └─> docs/TODO.md에서 최우선 태스크 선택
  └─> git checkout -b feature/<task-slug>
  └─> 구현
  └─> ./gradlew assembleDebug
      ├─ 실패 → 중단 및 보고
      └─ 성공 → git commit + push → gh pr create [Dev]
```

---

## Out of Scope

- 테스트 자동화 (빌드 검증만 수행)
- 슬랙/이메일 알림
- 스케줄 기반 자동 실행 (수동 커맨드 실행만)
- `/planner`와 `/developer` 외 추가 페르소나
