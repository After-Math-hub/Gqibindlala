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
export RESEND_API_KEY='set-this-in-your-shell-or-secret-manager'
export RESEND_FROM_EMAIL='onboarding@resend.dev'
export PUBLIC_APP_URL='http://localhost:5173'
export CLOUDINARY_CLOUD_NAME='your-cloud-name'
export CLOUDINARY_API_KEY='your-api-key'
export CLOUDINARY_API_SECRET='set-this-in-your-shell-or-secret-manager'
```

The first backend startup creates the initial administrator if `ADMIN_EMAIL` and `ADMIN_PASSWORD` are set and that login identifier does not already exist. Set passwords and API secrets as environment secrets; do not commit them. Once signed in at `/account`, create departments, inventory, and staff accounts. Customer registration is public, sends a verification link through Resend, and can only create customer-role accounts. Staff login IDs use the domain configured for their department and do not deliver email. Site staff/customer communication happens in each order's message thread.

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

## Provider integrations

- **Customer email verification:** [Resend API](https://resend.com/docs) sends single-use verification links. Configure `RESEND_API_KEY` and `RESEND_FROM_EMAIL`; the `onboarding@resend.dev` sender is for testing and production requires an authorized sender domain. Staff accounts do not send email.
- **Equipment images:** [Cloudinary Upload API](https://cloudinary.com/documentation/image_upload_api_reference) receives admin uploads through a short-lived signed request. Configure `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`. The secret remains backend-only.
- **South African checkout:** evaluate [PayFast](https://developers.payfast.co.za/) first; compare its current merchant and transaction fees with [Yoco](https://developer.yoco.com/). The app currently supports manual payment reference review only. Gateway payment must be confirmed by a verified webhook.
- **Authentication and data:** authentication is implemented in the existing Spring Security/JWT backend; PostgreSQL and Flyway persist accounts, departments, inventory, orders, and in-site conversations.
- **Authentication and data:** authentication is implemented in the existing Spring Security/JWT backend; PostgreSQL and Flyway persist accounts, departments, inventory, orders, and in-site conversations.

## Database

Flyway owns schema changes. `V1__init_schema.sql` creates the original catalogue and booking tables; `V2__accounts_and_order_workflow.sql` adds accounts, payment review, and fulfillment state; `V3__dynamic_departments.sql` converts fixed categories to departments; `V4__order_conversations.sql` adds in-site order messages and optional staff personal email; and `V5__customer_email_verification.sql` adds customer email verification tokens.
