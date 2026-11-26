# HKUST Marketplace submission docs

The combined document for a TA is [submission.md](submission.md). This page is the older checklist.

This folder collects the grading materials required by the brief.

- [Javadoc guide](javadoc-guide.md) — task-to-code map and how to generate HTML docs.
- [Testing report](testing-report.md) — JUnit results per task.
- [Coverage report](coverage-report.md) — JaCoCo branch coverage summary.
- [Screenshots](screenshots/) — execution captures for each portal/task.

## Submission checklist

- [x] `./mvnw test` passes with 0 failures/errors — latest run: 131 tests, 0 failures (see [testing-report.md](testing-report.md)).
- [x] JaCoCo branch coverage is above 65% — overall 67.7% (584 of 862). See [coverage-report.md](coverage-report.md).
- [ ] Screenshots added for buyer/seller/admin flows.
- [ ] Supplementary notes updated in `supplementary-notes.md` or a linked doc.
