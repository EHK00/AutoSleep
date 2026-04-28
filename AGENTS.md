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
