# Agent Automation System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `/planner`와 `/developer` Claude Code 슬래시 커맨드를 구현하여 브랜치 생성 → 작업 수행 → PR 생성을 자동화한다.

**Architecture:** `AGENTS.md`에 공통 규칙을 정의하고, `.claude/commands/`에 페르소나별 프롬프트 파일을 작성한다. 두 커맨드 모두 최신 `release/*` 브랜치를 베이스로 사용한다.

**Tech Stack:** Claude Code slash commands, GitHub CLI (`gh`), Gradle (`./gradlew assembleDebug`)

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `AGENTS.md` | Create | 브랜치 네이밍, PR 형식, 빌드 커맨드, TODO 우선순위 규칙 |
| `docs/TODO.md` | Create | 요구사항 목록 (초기 항목 포함) |
| `.claude/commands/planner.md` | Create | `/planner` 페르소나 프롬프트 |
| `.claude/commands/developer.md` | Create | `/developer` 페르소나 프롬프트 |

---

### Task 1: AGENTS.md 생성

**Files:**
- Create: `AGENTS.md`

- [ ] **Step 1: AGENTS.md 작성**

`AGENTS.md`를 프로젝트 루트에 아래 내용으로 작성한다.

```
# AutoSleep Agent Conventions

## Base Branch
All feature branches must start from the latest `release/*` branch.
Find it with:
  git branch -r | grep 'release/' | sort -V | tail -1

## Branch Naming
- Planner : feature/planning-YYYYMMDD
- Developer: feature/<task-slug>  (TODO 태스크명을 kebab-case로 변환)

## PR Format
- Planner title : [Planner] 요구사항 분석 및 TODO 업데이트 YYYYMMDD
- Developer title: [Dev] <태스크명>
- Body: 변경 사항 요약 + docs/TODO.md 링크

## Build Verification (developer only)
Run ./gradlew assembleDebug before committing implementation.
Stop and report cause if build fails. Do NOT commit on failure.

## TODO Priority
| Level | Criteria                     |
|-------|------------------------------|
| P0    | 크리티컬 버그 / 핵심 기능 누락 |
| P1    | 주요 기능 개선                |
| P2    | UX 개선 / 마이너 기능         |
| P3    | 리팩토링 / 기술 부채           |
```

- [ ] **Step 2: 커밋**

```bash
git add AGENTS.md
git commit -m "feat: add AGENTS.md with agent conventions"
```

---

### Task 2: docs/TODO.md 초기화

**Files:**
- Create: `docs/TODO.md`

- [ ] **Step 1: docs/ 디렉토리 생성**

```bash
mkdir -p docs
```

- [ ] **Step 2: docs/TODO.md 작성**

현재 앱에 구현된 기능(타이머, 분석, 설정, 위젯, 접근성 서비스, 미디어 컨트롤)을 기반으로 아래 초기 항목을 포함해 작성한다.

```markdown
# AutoSleep TODO

## 진행 중
- [ ] (없음)

## 대기 중
| 우선순위 | 태스크 | 설명 | 추가일 |
|---------|-------|------|-------|
| P1 | 타이머 종료 알림 커스터마이징 | 종료 시 진동/소리/메시지 옵션 설정 화면 추가 | 2026-04-28 |
| P1 | 프리셋 이름 편집 | 기존 타이머 프리셋 이름 수정 및 삭제 기능 | 2026-04-28 |
| P2 | 다크모드 지원 | 시스템 테마 자동 연동 (MaterialTheme.colorScheme) | 2026-04-28 |
| P2 | 위젯 UI 개선 | 남은 시간 표시 및 일시정지 버튼 추가 | 2026-04-28 |
| P3 | 단위 테스트 작성 | UseCase 및 ViewModel 테스트 커버리지 확보 | 2026-04-28 |

## 완료
- [x] 위젯 추가 (2026-04-20)
- [x] Analytics 추가 (2026-04-20)
- [x] 국제화(i18n) 적용 (2026-04-20)
```

- [ ] **Step 3: 커밋**

```bash
git add docs/TODO.md
git commit -m "feat: initialize TODO.md with backlog items"
```

---

### Task 3: .claude/commands/planner.md 생성

**Files:**
- Create: `.claude/commands/planner.md`

- [ ] **Step 1: commands 디렉토리 생성**

```bash
mkdir -p .claude/commands
```

- [ ] **Step 2: planner.md 작성**

`.claude/commands/planner.md`를 아래 내용으로 작성한다.  
`$ARGUMENTS`는 Claude Code가 커맨드 실행 시 자동으로 사용자 입력값으로 치환한다.

```
당신은 AutoSleep Android 앱의 프로덕트 플래너입니다.
AGENTS.md의 규칙을 준수하며 아래 단계를 순서대로 실행하세요.

## 입력값
$ARGUMENTS

입력값이 있으면 해당 방향을 중심으로 요구사항을 분석합니다.
입력값이 없으면 앱 전체를 기획자 관점에서 분석합니다.

---

## 실행 단계

### 1. 최신 release 브랜치 확인
다음 명령으로 최신 release 브랜치를 확인합니다:
  git fetch origin
  git branch -r | grep 'release/' | sort -V | tail -1

확인된 브랜치를 BASE_BRANCH로 기억합니다 (예: origin/release/v1.0.2).

### 2. 플래닝 브랜치 생성
  git checkout -b feature/planning-$(date +%Y%m%d) <BASE_BRANCH>

### 3. 코드베이스 분석
다음을 읽고 현재 앱 상태를 파악합니다:
- app/src/main/java/com/ekh/autosleep/presentation/ (화면 목록)
- app/src/main/java/com/ekh/autosleep/service/ (백그라운드 서비스)
- app/src/main/java/com/ekh/autosleep/data/ (데이터 레이어)
- docs/TODO.md (기존 요구사항)
- git log --oneline -20 (최근 작업 내역)

### 4. 기획 분석
기획자 관점에서 수행합니다:
- 현재 기능 대비 사용자 경험에서 빠진 부분 파악
- 각 항목에 AGENTS.md 기준으로 P0~P3 우선순위 부여
- 입력값이 있으면 해당 방향성의 구체적인 요구사항으로 세분화

### 5. docs/TODO.md 업데이트
- 신규 항목을 "대기 중" 테이블에 우선순위 순으로 추가
- 기존 항목의 우선순위가 잘못됐다면 재조정
- 중복 항목은 통합

### 6. 커밋 및 PR 생성
  git add docs/TODO.md
  git commit -m "docs: update TODO with planning analysis $(date +%Y%m%d)"
  git push -u origin HEAD
  gh pr create \
    --title "[Planner] 요구사항 분석 및 TODO 업데이트 $(date +%Y%m%d)" \
    --body "## 변경 사항
추가/수정된 요구사항 목록을 작성합니다.

## 참고
[docs/TODO.md](docs/TODO.md)"
```

- [ ] **Step 3: 커밋**

```bash
git add .claude/commands/planner.md
git commit -m "feat: add /planner slash command"
```

---

### Task 4: .claude/commands/developer.md 생성

**Files:**
- Create: `.claude/commands/developer.md`

- [ ] **Step 1: developer.md 작성**

`.claude/commands/developer.md`를 아래 내용으로 작성한다.

```
당신은 AutoSleep Android 앱의 시니어 Android 개발자입니다.
AGENTS.md의 규칙을 준수하며 아래 단계를 순서대로 실행하세요.

---

## 실행 단계

### 1. 최우선순위 태스크 선택
docs/TODO.md의 "대기 중" 테이블을 읽고
P0 → P1 → P2 → P3 순으로 가장 높은 우선순위 태스크 1개를 선택합니다.
선택한 태스크명을 TASK_NAME으로 기억합니다.

### 2. 최신 release 브랜치 확인
  git fetch origin
  git branch -r | grep 'release/' | sort -V | tail -1

확인된 브랜치를 BASE_BRANCH로 기억합니다.

### 3. feature 브랜치 생성
TASK_NAME을 영문 kebab-case로 변환하여 브랜치를 생성합니다.
예: "다크모드 지원" → feature/dark-mode-support

  git checkout -b feature/<task-slug> <BASE_BRANCH>

### 4. 태스크를 "진행 중"으로 이동
docs/TODO.md에서 선택한 태스크 행을 "대기 중" 테이블에서
"진행 중" 섹션의 체크박스 항목으로 이동합니다.

  git add docs/TODO.md
  git commit -m "docs: move <task-slug> to in-progress"

### 5. 태스크 구현
현재 코드베이스 패턴을 참고하여 구현합니다:
- Clean Architecture (data/domain/presentation 레이어) 준수
- Jetpack Compose UI 패턴 따르기
- Hilt DI 활용
- 기존 네이밍 컨벤션 따르기

### 6. 빌드 검증
  ./gradlew assembleDebug

빌드 실패 시: 즉시 중단하고 오류 원인과 해결 방안을 보고합니다. 커밋하지 않습니다.
빌드 성공 시: 다음 단계로 진행합니다.

### 7. 태스크를 "완료"로 이동
docs/TODO.md에서 "진행 중" 항목을 "완료" 섹션으로 이동합니다.
완료일자를 괄호 안에 추가합니다. 예: - [x] 다크모드 지원 (2026-04-28)

### 8. 커밋 및 PR 생성
  git add .
  git commit -m "feat: implement <task-slug>"
  git push -u origin HEAD
  gh pr create \
    --title "[Dev] <태스크명>" \
    --body "## 구현 내용
구현한 내용을 요약합니다.

## 변경된 파일
변경된 주요 파일 목록을 작성합니다.

## 참고
[docs/TODO.md](docs/TODO.md)"
```

- [ ] **Step 2: 커밋**

```bash
git add .claude/commands/developer.md
git commit -m "feat: add /developer slash command"
```

---

### Task 5: 파일 존재 검증

**Files:**
- Read: `AGENTS.md`
- Read: `docs/TODO.md`
- Read: `.claude/commands/planner.md`
- Read: `.claude/commands/developer.md`

- [ ] **Step 1: 4개 파일 존재 확인**

```bash
ls -la AGENTS.md docs/TODO.md .claude/commands/planner.md .claude/commands/developer.md
```

Expected output (4개 파일 모두 존재):
```
-rw-r--r--  ...  AGENTS.md
-rw-r--r--  ...  docs/TODO.md
-rw-r--r--  ...  .claude/commands/planner.md
-rw-r--r--  ...  .claude/commands/developer.md
```

- [ ] **Step 2: $ARGUMENTS 플레이스홀더 확인**

```bash
grep -n 'ARGUMENTS' .claude/commands/planner.md
```

Expected: `$ARGUMENTS` 가 포함된 라인 출력됨

- [ ] **Step 3: 빌드 커맨드 참조 확인**

```bash
grep -n 'assembleDebug' .claude/commands/developer.md
```

Expected: `./gradlew assembleDebug` 가 포함된 라인 출력됨

- [ ] **Step 4: 최종 커밋 상태 확인**

```bash
git log --oneline -6
git status
```

Expected:
- `git status` → `nothing to commit, working tree clean`
- 최근 커밋 6개에 Task 1~4의 커밋 메시지 포함
