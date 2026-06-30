# User Service

**Port:** 8082  
**Database:** PostgreSQL (`users_db`)  
**Base path:** `/api/v1/users`

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/register` | None | Create account, returns JWT |
| `POST` | `/login` | None | Authenticate, returns JWT |
| `GET` | `/me` | JWT | Current user profile |
| `PATCH` | `/update` | JWT | Update profile fields |
| `DELETE` | `/delete` | JWT | Delete account |

## Kafka events published

| Topic | Trigger |
|---|---|
| `user.created` | Successful registration — consumed by Notification Service |

## Database

Single `users` table managed by Flyway. R2DBC reactive driver.
