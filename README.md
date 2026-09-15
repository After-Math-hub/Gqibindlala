# Gqibindlala Catering & Tent Hiring

Booking and inventory system. Java (Spring Boot) backend, Vite + React + TypeScript frontend.

## Structure

```
gqibindlala/
├── backend/                        Spring Boot API (Java 21, Maven)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/gqibindlala/booking/
│       │   ├── model/               Equipment, BookingRequest, BookingItem, enums
│       │   ├── repository/          Spring Data JPA repositories
│       │   ├── service/             AvailabilityService, BookingService
│       │   ├── controller/          REST endpoints
│       │   ├── dto/                 Request payloads
│       │   ├── config/              Security config
│       │   └── BookingApplication.java
│       └── resources/
│           ├── application.yml
│           └── db/migration/        Flyway SQL migrations (V1__init_schema.sql)
│
└── frontend/                       Vite + React + TypeScript
    ├── index.html
    ├── vite.config.ts
    ├── tailwind.config.js
    └── src/
        ├── App.tsx                  Route structure (public site + /admin)
        ├── pages/                   HomePage, BookingPage, AdminDashboardPage
        ├── api/client.ts            Axios calls to the backend
        └── types/equipment.ts       Shared TS types
```

## Core model

- **Equipment** — chairs, tables, tents, mobile fridge, meat cutter. Has a total quantity and a price per unit per day.
- **BookingRequest** — one customer enquiry, holds a list of items.
- **BookingItem** — one equipment line within a booking: its own quantity, start date, end date, and status. Items are independent, so a booking can have chairs for 2 days and a tent for 5 days at once.

Availability is quantity-aware and only blocked by **confirmed** (paid) bookings — pending requests don't reserve stock. See `AvailabilityService.remainingQuantity()` and the query in `BookingItemRepository`.

## Running locally

**Backend** (needs Java 21, Maven, and a local PostgreSQL database named `gqibindlala`):
```bash
cd backend
mvn spring-boot:run
```

**Frontend**:
```bash
cd frontend
npm install
npm run dev
```
The Vite dev server proxies `/api` requests to `http://localhost:8080` (see `vite.config.ts`).

## Build order

1. Public site + equipment catalogue + booking form *(scaffolded)*
2. Equipment/inventory admin CRUD
3. Booking flow with per-item dates + quantity-aware availability *(scaffolded — core logic in place)*
4. Admin dashboard — orders, confirm/cancel, payment status

## Not yet built

- Admin login / JWT issuing endpoint (dependency is wired in, filter isn't)
- Admin dashboard UI (bookings list, confirm/cancel buttons, equipment CRUD forms)
- Booking calendar view showing blocked-out dates per item
- Payment integration
