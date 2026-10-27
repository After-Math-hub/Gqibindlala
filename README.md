# Gqibindlala Catering & Tent Hiring

Booking and inventory system. The backend is Java 21, Spring Boot, PostgreSQL, Spring Security, and Flyway. The frontend is Vite, React, and TypeScript.

## Order workflow

- Visitors can browse equipment without an account. Customers register or sign in before placing an order; name and phone are reused from their profile.
- Each order contains independently dated equipment items. The customer submits a payment reference after paying through the business's chosen payment channel.
- A `PAYMENTS` user reviews the reference. Approval confirms the order and reserves the equipment; rejection lets the customer submit a corrected reference.
- An administrator creates staff accounts. Department login IDs are generated from the employee's first initial and surname at that department's login domain (for example `jsmith@chair-gqi.co.za`). These are site login identifiers, not mailboxes; the app does not send employee email. An optional personal email is stored for future use only.
- Departments are database records that an administrator can create or deactivate. Equipment belongs to a department and is listed with its photo, stock quantity, and daily price. Department users see only their confirmed items and can update preparation status.
- Customers and staff can discuss an order in its in-site message thread. Customers only see their own orders; department users see messages for their assigned department, while payment/admin staff can review the overall conversation.
- `ADMIN` can review payments, manage departments and inventory, create staff accounts, and access the all-orders endpoint.

Order state, payment state, and department fulfillment state are stored separately. Pending and unpaid orders do not reserve stock. Only paid/confirmed orders reduce availability.

## Local setup

Create a PostgreSQL database named `gqibindlala`. Set credentials and secrets in the shell; do not commit them:

```bash
export DB_USERNAME=gqibindlala
export DB_PASSWORD='your-database-password'
export JWT_SECRET='at-least-32-random-bytes-for-local-development'
export ADMIN_EMAIL=admin@gqibindlala.co.za
export ADMIN_PASSWORD='a-unique-password-of-at-least-10-characters'
```

The first backend startup creates the initial administrator if `ADMIN_EMAIL` and `ADMIN_PASSWORD` are set and that login identifier does not already exist. Set the password as an environment secret; do not commit it. Once signed in at `/account`, create departments, inventory, and staff accounts. Customer registration is public and can only create customer-role accounts. Staff login IDs use the domain configured for their department and do not deliver email.

**Backend** (Java 21 and Maven):
```bash
cd backend
mvn spring-boot:run
```

**Frontend** (separate terminal):
```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` requests to `http://localhost:8080`.

## Payment integration

There is not yet a payment gateway integration. The current workflow accepts a payment reference and requires a staff member to verify it against the actual payment channel before approval. A live redirect/checkout requires selecting a provider and configuring its merchant credentials and callback/webhook URLs. Do not treat a customer-entered reference as proof of payment.

## Recommended API providers

- **Email verification for customer accounts:** [Resend API](https://resend.com/docs) is the recommended first option. [Brevo's API](https://developers.brevo.com/) is an alternative. Neither is currently wired into account registration; customer verification needs a sender domain and API key. Staff login IDs do not receive email.
- **South African checkout:** evaluate [PayFast](https://developers.payfast.co.za/) first; compare its current merchant and transaction fees with [Yoco](https://developer.yoco.com/). The app currently supports manual payment reference review only. Gateway payment must be confirmed by a verified webhook.
- **Equipment images:** [Cloudinary](https://cloudinary.com/documentation) is the recommended image API. Admin inventory currently requires a hosted image URL and previews it; direct authenticated uploads need Cloudinary account credentials and a signed-upload endpoint.
- **Authentication and data:** authentication is implemented in the existing Spring Security/JWT backend; PostgreSQL and Flyway persist accounts, departments, inventory, orders, and in-site conversations.

## Database

Flyway owns schema changes. `V1__init_schema.sql` creates the original catalogue and booking tables; `V2__accounts_and_order_workflow.sql` adds accounts, payment review, and fulfillment state; `V3__dynamic_departments.sql` converts fixed categories to departments; and `V4__order_conversations.sql` adds in-site order messages and optional staff personal email.
