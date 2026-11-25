# Coverage report

`./mvnw test` runs JaCoCo and writes the HTML report to `target/site/jacoco/index.html`. That folder is generated locally and is not committed.

From Matthew’s latest `index.html`:

| Scope | Instruction | Branch |
|---|---:|---:|
| Overall | 86% | 67% (574 of 848) |
| `services` | | 69% |
| `utils` | | 62% |

Overall branch coverage is 67%, which meets the requirement of more than 65%.
