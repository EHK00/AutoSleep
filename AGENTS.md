# AutoSleep Agent Conventions

## Base Branch
All feature branches must start from the latest `release/*` branch.
Find it with:
  git branch -r | grep 'release/' | sort -V | tail -1

## Branch Naming
- Planner   : feature/planning-YYYYMMDD
- Developer : feature/<task-slug>  (TODO 태스크명을 kebab-case로 변환)
- AI Expert : feature/ai-review-<topic>  (검토 주제를 kebab-case로 변환, 예: skills-migration, frontmatter)

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
- AI Expert title: [AI Expert] 에이전트 인프라 검토 및 개선 - <topic>

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

## Skill 구조
모든 페르소나는 `.claude/skills/<name>/SKILL.md` 형식으로 관리합니다:

```
.claude/skills/
├── ai-expert/
│   └── SKILL.md
├── developer/
│   └── SKILL.md
└── planner/
    └── SKILL.md
```

각 SKILL.md 상단에는 YAML frontmatter가 필수입니다:

```yaml
---
name: <skill-name>
description: <Claude 자동 호출 판단에 쓰이는 설명. 주요 사용 사례를 앞에 배치>
disable-model-invocation: true   # 부작용이 있는 모든 워크플로우에 필수
argument-hint: "[인수 설명 (optional)]"   # $ARGUMENTS를 받는 skill에만
allowed-tools: Bash(git *) Bash(gh *) Read Write Edit Glob Grep
---
```

- `disable-model-invocation: true`: Claude가 자동 실행하지 못하도록 강제. commit·PR 생성 등 부작용이 있는 모든 워크플로우에 필수
- `description`: Claude가 자동 호출 여부를 판단하는 기준. 핵심 트리거 키워드를 앞에 배치
- `allowed-tools`: 권한 프롬프트 없이 실행 가능한 도구 사전 승인

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

Worktree 충돌 처리 (동일 경로가 이미 존재할 경우):
  git worktree remove "<worktree-path>" 2>/dev/null || true
  git branch -d "<branch-name>" 2>/dev/null || true
  git worktree add "<worktree-path>" -b "<branch-name>" <BASE_BRANCH>

## PR Merge Process
- 각 페르소나가 생성한 PR은 GitHub에서 Squash and Merge로 병합합니다.
- 머지 후 release/* 브랜치에 반영되며, 다음 플래닝/개발 사이클의 BASE_BRANCH가 됩니다.
- Developer PR: 리뷰어가 승인한 뒤 머지. 머지 후 Developer가 로컬 worktree를 수동 정리.
- Planner / AI Expert PR: 별도 리뷰 없이 즉시 머지 가능 (docs/infra 변경만 포함).

## TODO Priority
| Level | Criteria                     |
|-------|------------------------------|
| P0    | 크리티컬 버그 / 핵심 기능 누락 |
| P1    | 주요 기능 개선                |
| P2    | UX 개선 / 마이너 기능         |
| P3    | 리팩토링 / 기술 부채           |
