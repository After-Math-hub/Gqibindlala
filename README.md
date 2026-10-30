# Gqibindlala Catering & Tent Hiring

Booking and inventory system. The backend is Java 21, Spring Boot, PostgreSQL, Spring Security, and Flyway. The frontend is Vite, React, and TypeScript.

## Order workflow

- Visitors can browse equipment without an account. Customers register or sign in before placing an order; name and phone are reused from their profile.
- Each order contains independently dated equipment items. The customer submits a payment reference after paying through the business's chosen payment channel.
- A `PAYMENTS` user reviews the reference. Approval confirms the order and reserves the equipment; rejection lets the customer submit a corrected reference.
- An administrator creates staff accounts. Department login IDs are generated from the employee's first initial and surname at that department's login domain (for example `jsmith@chair-gqi.co.za`). These are site login identifiers, not mailboxes; the app does not send employee email. An optional personal email is stored for future use only.
- Departments are database records that the supervisor can create or deactivate. Finance is an internal department and never appears in the customer catalogue. Equipment belongs to a sales/operations department and is listed with its photo, stock quantity, and daily price. Department staff can check date-specific remaining stock, prepare quotations at the approved price, and update preparation status after payment.
- Department staff propose new equipment, price changes, and stock changes. The main administrator reviews and approves or rejects each proposal; unapproved changes do not affect the customer catalogue or live prices. Staff/admin discuss proposals in an internal thread separate from customer order messages.
- Customers and staff can discuss an order in its in-site message thread. Customers only see their own orders; department users see messages for their assigned department, while payment/admin staff can review the overall conversation.
- `ADMIN` is the supervisor: they manage staff/departments, review and accept/reject department stock and price proposals, and access the all-orders endpoint. Live equipment changes are applied only after proposal approval; the supervisor does not add or edit inventory directly.
- At checkout, customers choose full payment or request a Finance discussion. Finance staff discuss options with the customer in the order thread and update the request status. This is not an automatic credit decision, loan, or instalment contract; those terms must be agreed outside the system and recorded by the Finance team.

Order state, payment state, and department fulfillment state are stored separately. Pending and unpaid orders do not reserve stock. Only paid/confirmed orders reduce availability.

## Local setup

Create a PostgreSQL database named `gqibindlala`. Set credentials and secrets in the shell; do not commit them:

```bash
export DB_USERNAME=gqibindlala
export DB_PASSWORD='your-database-password'
export JWT_SECRET='at-least-32-random-bytes-for-local-development'
export ADMIN_EMAIL=admin@gqibindlala.co.za
export ADMIN_PASSWORD='a-unique-password-of-at-least-10-characters'
export BREVO_API_KEY='set-this-in-your-shell-or-secret-manager'
export BREVO_FROM_EMAIL='verified-sender@example.com'
export BREVO_SENDER_NAME='Gqibindlala'
export PUBLIC_APP_URL='http://localhost:5173'
export CLOUDINARY_CLOUD_NAME='your-cloud-name'
export CLOUDINARY_API_KEY='your-api-key'
export CLOUDINARY_API_SECRET='set-this-in-your-shell-or-secret-manager'
```

The first backend startup creates the initial administrator if `ADMIN_EMAIL` and `ADMIN_PASSWORD` are set and that login identifier does not already exist. Set passwords and API secrets as environment secrets; do not commit them. Once signed in at `/account`, the supervisor creates departments and staff accounts, then reviews inventory proposals. Staff receive a single-use activation link at their personal email and set their own password; the generated department login identifier is included in that email. Customer registration is public, sends a verification link, and can only create customer-role accounts. Site staff/customer communication happens in each order's message thread.

For a Render deployment, add `ADMIN_PASSWORD` as a secret environment variable on the backend service, keep `ADMIN_EMAIL` set to the intended login (it defaults to `admin@gqibindlala.co.za`), and redeploy. The app creates the account only if that login identifier is not already in the database; it will not reset an existing account's password. Startup logs report whether the account was created, already existed, or could not be created because the password is missing.

If the existing administrator password must be reset, first deploy the migration that adds the one-time reset guard. Then set `ADMIN_PASSWORD` to the new secret and `ADMIN_PASSWORD_RESET_ONCE=true` on the Render backend, and redeploy. The existing account is reset only if it exists and has the `ADMIN` role. The database guard prevents subsequent application startups from resetting it again, even if the flag is accidentally left enabled. Remove `ADMIN_PASSWORD_RESET_ONCE` (or set it to `false`) after the reset deployment. Do not put the password in source control or logs.

**Backend** (Java 21 and Maven):
```bash
cd backend
if [[ -f ../.env ]]; then set -a; source ../.env; set +a; fi
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

- **Account email:** [Brevo Transactional Email API](https://developers.brevo.com/reference/send-transac-email) sends customer verification links and staff activation links through `POST https://api.brevo.com/v3/smtp/email` using the `api-key` header. On Render, set `BREVO_API_KEY` and `BREVO_FROM_EMAIL` on the backend service (not the frontend); the sender address must be verified in the same Brevo account. `BREVO_SENDER_NAME` is optional. Both link types are single-use and expire after 24 hours. On a send failure, backend logs include the HTTP status, Brevo error code/message, whether the API key is configured, sender identity, and whether an HTTP response was received; the API key and email link token are never logged. A missing API key or sender is logged as a configuration failure without attempting the request.
- **Equipment images:** [Cloudinary Upload API](https://cloudinary.com/documentation/image_upload_api_reference) receives admin uploads through a short-lived signed request. Configure `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`. The secret remains backend-only.
- **South African checkout:** evaluate [PayFast](https://developers.payfast.co.za/) first; compare its current merchant and transaction fees with [Yoco](https://developer.yoco.com/). The app currently supports manual payment reference review only. Gateway payment must be confirmed by a verified webhook.
- **Authentication and data:** authentication is implemented in the existing Spring Security/JWT backend; PostgreSQL and Flyway persist accounts, departments, inventory, orders, and in-site conversations.

## Database

Flyway owns schema changes. `V1__init_schema.sql` creates the original catalogue and booking tables; `V2__accounts_and_order_workflow.sql` adds accounts, payment review, and fulfillment state; `V3__dynamic_departments.sql` converts fixed categories to departments; `V4__order_conversations.sql` adds customer order messages; `V5__customer_email_verification.sql` adds verification tokens; `V6__inventory_change_approvals.sql` adds supervisor-reviewed inventory proposals; `V7__department_quotations.sql` adds per-line quotes; `V8__inventory_change_discussions.sql` adds internal staff/admin threads; `V9__staff_account_activation.sql` adds staff deactivation; and `V10__finance_checkout_requests.sql` adds internal-only Finance plus checkout request status.
