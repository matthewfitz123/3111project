# Javadoc guide

Javadoc comments live inside the Java sources as `/** ... */` blocks. They are not a separate document; tools such as `javadoc` or the IDE can render them.

To generate HTML docs locally:

```bash
./mvnw javadoc:javadoc
```

Output is written under `target/site/apidocs` (not committed).

## Task map

| Task | Main classes / methods | What the comments explain |
|---|---|---|
| 1.1 Buyer Registration | `services/RegistrationService#registerBuyer`<br>`controllers/buyer/BuyerRegisterController#handleRegister`<br>`utils/DummyDataService#usernameExists`, `#findUser`, `#registerUser`, `#checkPasswordStrength`<br>`utils/PasswordUtil#hash`, `#verify` | Validation order, unique username across all roles, password rules, hashed credential storage, optional residence hall. |
| 1.2 Buyer Login | `services/AuthService#loginBuyer`<br>`controllers/buyer/BuyerLoginController#handleLogin`<br>`utils/SessionManager#login`<br>`utils/PasswordUtil#verify` | Required fields, case-insensitive username lookup, buyer-role check, PBKDF2 password verification, active-account check and session creation. |
