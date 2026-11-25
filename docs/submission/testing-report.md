# Testing report

## How to run

```bash
./mvnw test
```

JUnit 5 tests run through Maven Surefire. JaCoCo is attached for coverage.

## Latest result

- Date: 2026-11-25
- Command: `./mvnw test`
- Tests run: **84**
- Failures: **0**
- Errors: **0**
- Skipped: **0**
- Result: **BUILD SUCCESS**

## Full suite

| Test class | Tests |
|---|---:|
| `services.AuthServiceTest` | 8 |
| `services.BorrowSchedulerTest` | 2 |
| `services.DealHistoryServiceTest` | 3 |
| `services.DealServiceTest` | 4 |
| `services.ListingDealEdgeTest` | 6 |
| `services.ListingServiceTest` | 6 |
| `services.NotificationEdgeTest` | 3 |
| `services.NotificationServiceTest` | 4 |
| `services.OfferServiceEdgeTest` | 7 |
| `services.OfferServiceTest` | 9 |
| `services.RegistrationServiceTest` | 5 |
| `utils.ChatStoreTest` | 5 |
| `utils.DummyDataServiceTest` | 8 |
| `utils.PasswordRulesTest` | 5 |
| `utils.PasswordUtilTest` | 5 |
| `utils.SessionManagerTest` | 4 |
| **Total** | **84** |

## Task 1.1 Buyer Registration

Added `services/RegistrationServiceTest` for the registration rules, and reused the existing password/store tests for the shared pieces.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.RegistrationServiceTest` | 5 | Username/full-name validation, password strength and confirmation match, duplicate username rejection across buyer/seller/admin, salted hash storage, optional residence hall. |
| `utils.PasswordRulesTest` | 5 | Shared password rules used by registration: length limits and required character mix. |
| `utils.PasswordUtilTest` | 5 | Salted PBKDF2 hash/verify behavior used to store credentials. |
| `utils.DummyDataServiceTest` | 8 | In-memory user store basics and seeded account roles used by registration/login checks. |

The JavaFX form itself is thin: `BuyerRegisterController#handleRegister` delegates to `RegistrationService` and only displays success/error labels. Screen behavior is verified manually and by screenshots.

## Task 1.2 Buyer Login

Added `services/AuthServiceTest` for the buyer login rules, and reused the session/password tests for the shared pieces.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.AuthServiceTest` | 8 | Required fields, case-insensitive username lookup, buyer-role rejection for seller/admin credentials, wrong password, deactivated account, session creation, and login for a newly registered buyer. |
| `utils.PasswordUtilTest` | 5 | Salted PBKDF2 verify behavior used during login. |
| `utils.SessionManagerTest` | 4 | Session login/logout and role guard helpers used after authentication. |

The JavaFX login screen is thin: `BuyerLoginController#handleLogin` delegates to `AuthService#loginBuyer` and only displays errors or navigates to the buyer dashboard. Screen behavior is verified manually and by screenshots.
