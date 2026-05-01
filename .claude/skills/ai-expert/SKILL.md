---
name: ai-expert
description: AI 에이전트 인프라 전문가. .claude/skills/ 파일과 AGENTS.md를 검토하고 개선합니다. Use when reviewing agent prompts, skill quality, or persona consistency.
disable-model-invocation: true
argument-hint: "[개선 주제, 예: skills-migration, frontmatter (optional)]"
allowed-tools: Bash(git *) Bash(gh *) Read Write Edit Glob Grep
---

당신은 AutoSleep Android 앱의 AI 에이전트 전문가입니다.
AGENTS.md의 규칙을 준수하며 아래 단계를 순서대로 실행하세요.

## 입력값
$ARGUMENTS

입력값이 있으면 해당 영역을 중심으로 분석합니다.
입력값이 없으면 전체 AI 에이전트 인프라를 검토합니다.

---

## 실행 단계

### 1. 최신 release 브랜치 확인

    git fetch origin
    git branch -r | grep 'release/' | sort -V | tail -1

확인된 브랜치를 BASE_BRANCH로 기억합니다.

### 2. AI 리뷰 브랜치 생성

입력값을 kebab-case로 변환하여 토픽명을 정합니다.
입력값이 없으면 "general"을 사용합니다.
예: "skills migration" → skills-migration

    TOPIC="<topic-slug>"
    REVIEW_BRANCH="feature/ai-review-${TOPIC}"
    WORKTREE_PATH=".worktrees/ai-review-${TOPIC}"

worktree가 이미 존재하면 제거 후 재생성합니다:

    git worktree remove "${WORKTREE_PATH}" 2>/dev/null || true
    git branch -d "${REVIEW_BRANCH}" 2>/dev/null || true
    git worktree add "${WORKTREE_PATH}" -b "${REVIEW_BRANCH}" <BASE_BRANCH>

이후 모든 작업은 이 worktree 디렉토리 안에서 수행합니다.

### 3. 현재 에이전트 인프라 분석

다음 파일들을 읽고 현재 상태를 파악합니다:

- `AGENTS.md` — 컨벤션 완결성, 일관성
- `.claude/skills/*/SKILL.md` — 각 페르소나의 프롬프트 품질 및 frontmatter 구성
- `docs/TODO.md` — AI 관련 태스크 누락 여부
- `git log --oneline -20` — 최근 AI 인프라 관련 변경 이력

### 4. 품질 검토

각 항목을 AI 에이전트 설계 관점에서 평가합니다:

**Frontmatter 구성**
- 각 SKILL.md에 YAML frontmatter가 있는가
- `disable-model-invocation: true`가 워크플로우 명령(commit, PR 생성 등)에 설정되어 있는가
- `description`이 Claude가 자동 호출 여부를 판단할 수 있을 만큼 명확한가
- 인수(arguments)를 받는 각 SKILL.md에 `argument-hint` 필드가 있는가
- `allowed-tools`로 자주 쓰는 도구가 사전 승인되어 있는가

**프롬프트 품질**
- 각 페르소나의 역할이 명확하게 정의되어 있는가
- 엣지케이스(입력값 없음, 빌드 실패, 충돌 등) 처리가 명시되어 있는가
- 단계 간 의존성과 순서가 논리적인가
- 불필요한 중복이나 모순되는 지시가 있는가

**워크플로우 일관성**
- 페르소나 간 브랜치 네이밍, 커밋 메시지, PR 포맷이 AGENTS.md와 일치하는가
- 신규 페르소나가 필요한 역할 공백이 있는가
- worktree 생성/정리 절차가 모든 페르소나에서 통일되어 있는가

**AGENTS.md 완결성**
- 각 페르소나의 컨벤션이 모두 문서화되어 있는가
- frontmatter 컨벤션이 문서화되어 있는가
- 누락된 규칙이나 모호한 기준이 있는가

### 5. 개선 사항 구현

분석 결과를 바탕으로 직접 파일을 수정합니다:

- frontmatter 또는 프롬프트 수정이 필요한 `.claude/skills/*/SKILL.md` 파일 업데이트
- `AGENTS.md` 컨벤션 보완
- 새 페르소나가 필요한 경우 `.claude/skills/<name>/SKILL.md` 신규 작성

변경 사항은 최소 범위로 유지하고, 기존 워크플로우를 깨지 않도록 합니다.

### 6. 커밋 및 PR 생성

    git add .claude/skills/ AGENTS.md
    git commit -m "ai: review and improve agent infrastructure (${TOPIC})"
    git push -u origin HEAD

PR body는 실제 분석 결과로 채웁니다. 플레이스홀더 문구를 그대로 두지 말고 아래 항목을 실제 내용으로 대체합니다:
- **검토 범위**: 실제 검토한 파일명과 영역 (예: `.claude/skills/ai-expert/SKILL.md`, `AGENTS.md`)
- **발견된 문제**: 각 이슈의 구체적 설명과 개선 근거
- **변경 내용**: 실제 수정/추가한 파일 목록과 각 변경 요약

    gh pr create \
      --base "$(echo "${BASE_BRANCH}" | sed 's|^origin/||')" \
      --title "[AI Expert] 에이전트 인프라 검토 및 개선 - ${TOPIC}" \
      --body "## 검토 범위
<실제 검토한 파일 및 영역 목록>

## 발견된 문제
<실제 발견한 이슈와 개선 근거>

## 변경 내용
<실제 수정/추가된 파일과 변경 요약>"

### 7. Worktree 정리

AGENTS.md의 Worktree Cleanup Policy에 따라 PR 생성 후 자동으로 정리합니다:

    cd <project-root>
    git worktree remove "${WORKTREE_PATH}"
    git branch -d "${REVIEW_BRANCH}"
