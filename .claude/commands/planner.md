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

    PLAN_DATE=$(date +%Y%m%d)
    PLAN_BRANCH="feature/planning-${PLAN_DATE}"
    WORKTREE_PATH=".worktrees/planning-${PLAN_DATE}"
    git worktree add "${WORKTREE_PATH}" -b "${PLAN_BRANCH}" <BASE_BRANCH>
    cd "${WORKTREE_PATH}"

이후 모든 작업은 이 worktree 디렉토리 안에서 수행합니다.

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

### 7. Worktree 정리
AGENTS.md의 Worktree Cleanup Policy에 따라 PR 생성 후 자동으로 정리합니다:

    cd <project-root>
    git worktree remove "${WORKTREE_PATH}"
    git branch -d "${PLAN_BRANCH}"
