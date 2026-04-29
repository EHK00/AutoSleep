# AutoSleep Agent Conventions

## Base Branch
All feature branches must start from the latest `release/*` branch.
Find it with:
  git branch -r | grep 'release/' | sort -V | tail -1

## Branch Naming
- Planner   : feature/planning-YYYYMMDD
- Developer : feature/<task-slug>  (TODO 태스크명을 kebab-case로 변환)
- AI Expert : feature/ai-review-YYYYMMDD

## Commit Message Prefix
| Prefix  | 용도                              |
|---------|-----------------------------------|
| feat:   | 새 기능 구현                       |
| fix:    | 버그 수정                          |
| docs:   | 문서·TODO 변경                     |
| refactor: | 리팩토링 (기능 변경 없음)          |
| test:   | 테스트 추가·수정                   |
| ai:     | AI 에이전트 인프라 변경            |
| chore:  | 빌드·설정·기타                     |

## PR Format
- Planner title  : [Planner] 요구사항 분석 및 TODO 업데이트 YYYYMMDD
- Developer title: [Dev] <태스크명>
- AI Expert title: [AI Expert] 에이전트 인프라 검토 및 개선 YYYYMMDD

### Planner PR body template
## 변경 사항
<추가/수정된 요구사항 목록>

## 참고
[docs/TODO.md](docs/TODO.md)

### Developer PR body template
## 구현 내용
<구현한 내용 요약>

## 변경된 파일
<변경된 주요 파일 목록>

## 참고
[docs/TODO.md](docs/TODO.md)

### AI Expert PR body template
## 검토 범위
<검토한 파일 및 영역>

## 발견된 문제
<이슈와 개선 근거>

## 변경 내용
<수정/추가된 파일과 변경 요약>

## Build Verification (developer only)
Run ./gradlew assembleDebug before committing implementation.
Stop and report cause if build fails. Do NOT commit on failure.

## Worktree Cleanup Policy
- **Planner / AI Expert**: worktree를 PR 생성 직후 자동 정리합니다.
- **Developer**: 리뷰 피드백 반영 및 머지 완료 후 수동 정리합니다.

정리 명령:
  cd <project-root>
  git worktree remove "<worktree-path>"
  git branch -d "<branch-name>"

## TODO Priority
| Level | Criteria                     |
|-------|------------------------------|
| P0    | 크리티컬 버그 / 핵심 기능 누락 |
| P1    | 주요 기능 개선                |
| P2    | UX 개선 / 마이너 기능         |
| P3    | 리팩토링 / 기술 부채           |
