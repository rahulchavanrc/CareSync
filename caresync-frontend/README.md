# CareSync — Frontend

React (Vite) client for the CareSync telehealth platform. Talks to the Spring Boot
backend via JWT-authenticated REST calls.

## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Point it at your backend (defaults to `http://localhost:8080` if you skip this):
   ```bash
   cp .env.example .env
   ```

3. Run it:
   ```bash
   npm run dev
   ```
   Opens on `http://localhost:5173`.

## What's here

- **Auth** (`src/context/AuthContext.jsx`) — JWT stored in `localStorage`, decoded role
  drives which routes/nav items are visible. `src/api/client.js` attaches the token to
  every request via an Axios interceptor and bounces to `/login` on a 401.
- **Routing** (`src/App.jsx`) — role-gated routes via `ProtectedRoute`.
- **Pages**:
  - `/` — landing page
  - `/login`, `/register` — auth (patients self-register; doctors are onboarded by an admin)
  - `/doctors` (patient) — search/browse doctors, book a slot
  - `/appointments` (patient) — booking history with live status
  - `/records` (patient) — own medical records only
  - `/schedule` (doctor) — manage requests, confirm/cancel/complete, add records
  - `/admin/doctors` (admin) — create doctor accounts
- **Design system** (`src/styles.css`) — CSS custom properties for the color/type/spacing
  tokens; no framework dependency.

## Notes

- The backend's CORS config (`SecurityConfig.corsConfigurationSource`) already allows
  `http://localhost:5173`, so no proxy config is needed in dev.
- There's no admin signup flow by design — seed your first admin directly in the
  `users` table (BCrypt-hash the password) or add a one-off `CommandLineRunner` on
  the backend for your environment.
