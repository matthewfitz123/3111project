# Coverage report

The combined document for a TA is [submission.md](submission.md). The percentages below are an earlier run. The fresh numbers are in that file.

`./mvnw test` runs JaCoCo and writes the HTML report to `target/site/jacoco/index.html`. That folder is generated locally and is not committed.

From Matthew’s latest `index.html`:

| Scope | Instruction | Branch |
|---|---:|---:|
| Overall | 86.5% | 67.7% (584 of 862) |
| `services` | | 69.6% |
| `utils` | | 62.7% |

Overall branch coverage is 67.7%, which meets the requirement of more than 65%.
