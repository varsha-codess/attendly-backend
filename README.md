# Attendly — Backend

Spring Boot backend for **Attendly**, a real-time classroom attendance system that prevents proxy attendance using time-rotating QR tokens and GPS geofencing.

## The problem this solves

Traditional attendance methods (roll call, sign-in sheets, static QR codes) are easy to game — a student can mark a friend present without being in class. Attendly closes this gap two ways:

1. **Rotating tokens** — the QR code a teacher displays encodes a token that changes automatically every N seconds. A screenshot or photo of the code becomes worthless almost immediately.
2. **Geofencing** — a student's scan is only accepted if their device's GPS coordinates fall within a configurable radius of the classroom, calculated using the Haversine formula.

## Tech stack

- **Java 21 / Spring Boot 4** — REST API
- **Spring Security** — CORS + (extensible to) JWT-secured endpoints
- **Spring Data JPA + Hibernate** — ORM, auto schema generation
- **MySQL** — persistence
- **JJWT** — JSON Web Token generation/validation
- **BCrypt** — password hashing

## Core features

- **Auth** — register/login with hashed passwords, JWT issued on login
- **Sessions** — a teacher starts a session with a classroom's GPS coordinates + allowed radius
- **Token rotation** — a scheduled job (`@Scheduled`) regenerates each active session's token via HMAC-SHA256 every N seconds
- **Attendance validation** — a submitted scan is checked against: token freshness, geofence distance, and duplicate submission, with every attempt (accepted or rejected) logged with a reason

## API overview

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/auth/register` | POST | Create a user (student or teacher) |
| `/api/auth/login` | POST | Authenticate, returns JWT |
| `/api/sessions/start` | POST | Teacher starts a class session |
| `/api/sessions/{id}/current-token` | GET | Fetch the currently valid rotating token |
| `/api/sessions/{id}/stop` | POST | End a session |
| `/api/attendance/mark` | POST | Student submits a scanned token + GPS location |
| `/api/attendance/session/{id}` | GET | View all attendance records for a session |

## Running locally

1. Create a MySQL database named `attendly_db`
2. Update `src/main/resources/application.properties` with your MySQL credentials
3. Run: `./mvnw spring-boot:run` (or run `AttendlyBackendApplication` from your IDE)
4. Server starts on `http://localhost:8080`

## Related repo

Frontend (React): [attendly-frontend](https://github.com/varsha-codess/attendly-frontend)