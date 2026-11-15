# Aivya School Management -- Teacher App

Teacher-side school management app: sign in, view assigned classes and
rosters, and take daily attendance either manually or by scanning each
student's ID-card QR code. Built against a real backend -- no hard-coded
business data in the screens.

- **android/** -- Kotlin + Jetpack Compose app
- **backend/** -- Spring Boot (Java 21) REST API ([backend/README.md](backend/README.md))

## Stack
Kotlin, Jetpack Compose, Retrofit, CameraX + ML Kit (QR scanning),
EncryptedSharedPreferences (secure token storage) · Spring Boot, Spring
Security (stateless JWT), Spring Data JPA, H2.

## Demo credentials
- Email: `teacher@aivya.edu`
- Password: `AivyaDemo@2026`

Synthetic demo classes and students are seeded automatically on backend
startup -- no real student data is used anywhere in this project.

## Live demo backend
Deployed on Render's free tier: https://aivya-school-management-application.onrender.com
(the Android app's `API_BASE_URL` already points here, so the APK works on
any phone with internet -- no local setup needed to try it). Free instances
spin down after inactivity; the first request after a while can take up to
~50s to wake it back up, which the app surfaces as a loading state rather
than hanging silently.

## Running it locally
1. Backend: `cd backend && ./mvnw spring-boot:run` (starts on `:8080`).
2. Android: open `android/` in Android Studio. To point it at your local
   backend instead of the live one, change `API_BASE_URL` in
   `android/app/src/main/java/com/aivya/schoolmanagement/network/ApiClient.kt`
   to `http://10.0.2.2:8080/` (emulator) or your machine's LAN IP (real device).

## Security notes
- No secrets are committed. `backend/.env.example` lists what to set;
  the JWT signing key and demo password both come from environment
  variables with safe-for-demo defaults.
- JWT-protected API: a teacher can only read or mark attendance for their
  own assigned classes (enforced server-side).
- The app's session token is stored in `EncryptedSharedPreferences`
  (AES-256-GCM via the Android Keystore), never in plain text or logs.

## Known limitations
- H2 is in-memory, so backend data resets on restart (demo data reseeds automatically).
- School notices are static demo content on the client; there's no notices API yet.
