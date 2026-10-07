# CareSync — Telehealth & Appointment Booking (Backend)

Spring Boot REST API for the CareSync platform: patient/doctor auth with JWT,
appointment booking with overlap prevention, and post-consultation medical records.

## Stack
- Java 17, Spring Boot 3.3 (Web, Security, Data JPA, Validation)
- MySQL 8
- JWT (jjwt 0.12)
- JUnit 5 + Mockito

## Setup

1. **Create the database** (or let Hibernate do it — `createDatabaseIfNotExist=true` is set):
   ```sql
   CREATE DATABASE caresync_db;
   ```

2. **Set credentials** via environment variables (defaults to `root`/`root`):
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=yourpassword
   ```

3. **Run it:**
   ```bash
   mvn spring-boot:run
   ```
   The API starts on `http://localhost:8080`.

4. **Run tests:**
   ```bash
   mvn test
   ```

There's no admin bootstrap endpoint by design (admins create doctors, and admins
themselves should be seeded directly in the `users` table with a BCrypt-hashed
password, or via a one-off `CommandLineRunner` you add for your environment).

## Key Endpoints

| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | public | Patient self-registration |
| POST | `/api/auth/login` | public | Login, returns JWT |
| POST | `/api/admin/doctors` | ADMIN | Onboard a doctor |
| GET | `/api/doctors?specialization=Cardiologist` | any authenticated | Search doctors |
| POST | `/api/appointments/patient/book` | PATIENT | Book a slot |
| GET | `/api/appointments/patient/me` | PATIENT | My appointments |
| GET | `/api/appointments/doctor/schedule` | DOCTOR | My schedule |
| PATCH | `/api/appointments/doctor/{id}/status` | DOCTOR | Confirm/complete/cancel |
| POST | `/api/records` | DOCTOR | Add record (after COMPLETED) |
| GET | `/api/records/me` | PATIENT | My records only |
| POST | `/api/reviews` | PATIENT | Review a COMPLETED appointment (once) |
| GET | `/api/doctors/{id}/reviews` | any authenticated | List a doctor's reviews |

All protected routes require `Authorization: Bearer <token>`.

## Design notes

- **Availability check** lives in `util/AppointmentTimeUtil` as pure logic (no DB
  calls), so it's unit-testable in isolation and dialect-independent. The service
  fetches a doctor's same-day appointments and runs the check in Java.
- **Privacy**: `MedicalRecordService.getRecordById` only allows the owning patient
  or the treating doctor to view a record; everyone else gets 403.
- **Status transitions** are validated in `AppointmentService.validateTransition`
  (PENDING → CONFIRMED/CANCELLED, CONFIRMED → COMPLETED/CANCELLED, terminal states
  cannot change) — enforced with a 409 Conflict via `GlobalExceptionHandler`.
- See `docs/schema.sql` for the reference schema (Hibernate `ddl-auto=update`
  creates/evolves it automatically at startup, so this is documentation only).
