# Online Banking System — Spring Boot + Maven

A full stack backend for an online banking application: account creation, deposits/withdrawals,
fund transfers, transaction history, and JWT-based authentication with role-based access
(`CUSTOMER` vs `ADMIN`).

## Tech Stack
- Java 17
- Spring Boot 3.3 (Web, Data JPA, Security, Validation)
- Spring Security + JWT (jjwt)
- Hibernate / JPA
- H2 (default, in-memory, zero setup) or MySQL (production-style)
- Maven

## Project Structure
```
src/main/java/com/chandravijay/banking/
├── config/          SecurityConfig, DataSeeder
├── controller/       AuthController, AccountController, TransactionController, AdminController
├── dto/              Request/response payloads with validation annotations
├── entity/           User, Account, Transaction (+ enums)
├── exception/         Custom exceptions + GlobalExceptionHandler
├── repository/        Spring Data JPA repositories
├── security/          JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService, entry point
└── service/           Interfaces + impl (business logic, ownership checks, balance rules)
```

## Running it

### Option A — H2 in-memory (default, no setup needed)
```bash
mvn spring-boot:run
```
The app starts on `http://localhost:8080`. Data resets every restart.
H2 console (if you want to inspect tables): `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:bankingdb`, user `sa`, no password)

### Option B — MySQL
1. In `src/main/resources/application.properties`, comment out the H2 block and uncomment the MySQL block.
2. Set your MySQL username/password.
3. Run:
```bash
mvn spring-boot:run
```
The database `banking_db` is created automatically on first run.

### Build a runnable jar
```bash
mvn clean package
java -jar target/online-banking-system-1.0.0.jar
```

## Default admin account
On first startup, a seed admin is created automatically:
```
username: admin
password: admin123
```
**Change or remove this in `DataSeeder.java` before any real deployment.**

## API Reference

All endpoints are prefixed `/api`. Protected endpoints require:
```
Authorization: Bearer <jwt-token>
```

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new customer |
| POST | `/api/auth/login` | Public | Login, returns JWT |

**Register example**
```json
POST /api/auth/register
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123",
  "fullName": "John Doe"
}
```

**Login example**
```json
POST /api/auth/login
{
  "username": "johndoe",
  "password": "secret123"
}
```
Response:
```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "username": "johndoe",
  "role": "ROLE_CUSTOMER"
}
```

### Accounts
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/accounts` | Authenticated | Open a new account (SAVINGS/CURRENT) |
| GET | `/api/accounts/my` | Authenticated | List my accounts |
| GET | `/api/accounts/{id}` | Owner or Admin | Get one account |
| POST | `/api/accounts/{id}/deposit` | Owner or Admin | Deposit funds |
| POST | `/api/accounts/{id}/withdraw` | Owner or Admin | Withdraw funds |
| DELETE | `/api/accounts/{id}` | Owner or Admin | Close account (balance must be 0) |

```json
POST /api/accounts
{
  "accountType": "SAVINGS",
  "openingBalance": 1000.00
}
```
```json
POST /api/accounts/1/deposit
{
  "amount": 250.00,
  "description": "Salary credit"
}
```

### Transactions
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/transactions/transfer` | Authenticated (must own source account) | Transfer between accounts |
| GET | `/api/transactions/account/{accountId}` | Owner or Admin | Transaction history for an account |

```json
POST /api/transactions/transfer
{
  "fromAccountNumber": "AC1720000000001",
  "toAccountNumber": "AC1720000000002",
  "amount": 500.00,
  "description": "Rent"
}
```

### Admin (requires `ROLE_ADMIN`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/accounts` | List every account in the bank |
| GET | `/api/admin/users` | List every registered user |

## Error format
All errors return a consistent JSON body via a global exception handler:
```json
{
  "timestamp": "2026-07-11T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance in account AC1720000000001",
  "path": "/api/accounts/1/withdraw",
  "fieldErrors": null
}
```
Validation errors additionally populate `fieldErrors` with a `{field: message}` map.

## Notes / next steps
- Passwords are hashed with BCrypt; JWT is signed with HS256 (change `app.jwt.secret` before deploying).
- `spring.jpa.hibernate.ddl-auto=update` is convenient for development; use Flyway/Liquibase migrations for production.
- The `.gitignore` excludes `target/` — run `mvn clean package` to regenerate build artifacts.

## Frontend (React + Vite)

A matching React frontend lives in `frontend/` — login/register, an accounts dashboard, per-account
deposit/withdraw + transaction history, a transfer page, and an admin panel (all accounts/users).
It reuses the dark "ledger" visual style from the portfolio site.

### Run it
```bash
cd frontend
npm install
npm run dev
```
Opens on `http://localhost:5173`. In dev mode, Vite proxies any `/api/*` request to
`http://localhost:8080` (see `vite.config.js`), so make sure the Spring Boot backend is running first.

### Build for production
```bash
npm run build
```
Outputs static files to `frontend/dist/` — deploy behind any static host or serve them
from the Spring Boot app itself (copy `dist/*` into `src/main/resources/static/`).

### Structure
```
frontend/src/
├── api/            axios instance (JWT interceptor) + typed service calls
├── context/        AuthContext — login state, JWT storage, role check
├── components/      NavBar, ProtectedRoute
└── pages/           Login, Register, Dashboard, AccountDetail, Transfer, Admin
```

### Login
- Register a new customer via the UI, or sign in with the seeded admin (`admin` / `admin123`) to see the Admin panel.

