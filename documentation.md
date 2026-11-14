# Task 1.1 / 1.2 — Registration and Login

## Password rules (implemented)

10–64 characters
at least one uppercase letter
at least one lowercase letter
at least one digit
the confirmation password must match exactly


usernames must be unique across buyers, sellers and admins 
full name is required
residence hall is optional. 
all these checks run in
`BuyerRegisterController` before an account is created. can change this later

## Password storage

passwords are stored as salted PBKDF2-HMAC-SHA256 hashes (120,000 iterations
and 16 byte salt). `PasswordUtil.hash(...)` is used on registration and
`PasswordUtil.verify(...)` on login
the raw password is never actually stored
https://en.wikipedia.org/wiki/PBKDF2
the seller and admin login screens use the same verification so the seeded accounts keep working

user data is still held in memory by `DummyDataService`, so accounts are reset
when the application closes, like the rest of the skeleton atm

can replace it later without changing the registration/login screens

## Demo accounts
got ones for admin and seller but not really bothered on these rn
| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin12345` |
| Buyer | `alice` | `Password12` |
| Buyer | `bob` | `Password12` |
| Seller | `charlie` | `Password12` |
| Seller | `diana` | `Password12` |
