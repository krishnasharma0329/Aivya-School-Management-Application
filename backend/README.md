# Aivya School Management -- Backend

Spring Boot (Java 21) REST API for the Aivya teacher-side Android app.

## Stack
- Spring Boot 4, Spring Security (stateless JWT), Spring Data JPA
- H2 in-memory database (resets on restart; demo data auto-reseeds)
- JWT via jjwt

## Run locally
```bash
./mvnw spring-boot:run
```
Server starts on `http://localhost:8080`.

## Demo credentials
A demo teacher, 3 classes and their students are seeded automatically on
startup (synthetic data only -- no real student information).

- Email: `teacher@aivya.edu`
- Password: `AivyaDemo@2026`

Override with env vars `BOOTSTRAP_TEACHER_EMAIL` / `BOOTSTRAP_TEACHER_PASSWORD` /
`BOOTSTRAP_TEACHER_NAME` for a different account.

## API
All endpoints except `/api/v1/health` and `/api/v1/auth/login` require
`Authorization: Bearer <token>`.

- `POST /api/v1/auth/login` -- `{ email, password }` -> JWT
- `GET /api/v1/classes` -- the signed-in teacher's classes
- `GET /api/v1/classes/{classId}/roster` -- today's attendance per student
- `POST /api/v1/classes/{classId}/attendance` -- `{ records: [{studentId, status}] }`
- `POST /api/v1/classes/{classId}/attendance/qr-checkin` -- `{ qrCode }` marks that student present
- `GET /api/v1/classes/{classId}/attendance/history?days=7`

A teacher can only ever see or mark attendance for their own assigned classes
(enforced server-side, not just hidden in the UI).

## Deploying (Render, free tier)
This repo includes a `Dockerfile` and `render.yaml`:
1. Push this repo to GitHub.
2. On Render: New -> Blueprint -> pick this repo -> Apply.
3. Render builds the Docker image and gives you a public HTTPS URL.
4. Put that URL into the Android app's `API_BASE_URL`
   (`app/src/main/java/com/aivya/schoolmanagement/network/ApiClient.kt`) and rebuild the APK.

## Known limitations
- H2 is in-memory: data resets whenever the service restarts (fine for a demo/assessment).
- No notices/timetable backend yet -- the app shows an empty state for those.
