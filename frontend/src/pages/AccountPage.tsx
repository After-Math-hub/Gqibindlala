import { FormEvent, useEffect, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import {
  createStaffAccount,
  createDepartment,
  fetchDepartments,
  fetchDepartmentOrders,
  fetchMyBookings,
  fetchPaymentQueue,
  issueQuotation,
  reviewPayment,
  submitPaymentReference,
  setDepartmentActive,
  updateFulfillment,
} from "../api/client";
import { useAuth } from "../components/AuthProvider";
import {
  AppRole,
  BookingRecord,
  FulfillmentStatus,
  Department,
} from "../types/equipment";
import OrderConversation from "../components/OrderConversation";
import DepartmentInventoryWorkspace from "../components/DepartmentInventoryWorkspace";
import AdminInventoryApprovals from "../components/AdminInventoryApprovals";
import StaffDirectory from "../components/StaffDirectory";
import FinanceRequestQueue from "../components/FinanceRequestQueue";

const fulfillmentOptions: { value: FulfillmentStatus; label: string }[] = [
  { value: "NOT_STARTED", label: "Not started" },
  { value: "PREPARING", label: "Preparing" },
  { value: "READY", label: "Ready" },
  { value: "FULFILLED", label: "Fulfilled" },
];

function money(value: number) {
  return `R${Number(value).toFixed(2)}`;
}

export default function AccountPage() {
  const { user, loading } = useAuth();
  const [searchParams] = useSearchParams();

  if (loading) return <div className="account-loading">Opening your account…</div>;
  if (!user) return <AuthPanel redirectTo={searchParams.get("return") || "/account"} />;
  if (user.role === "CUSTOMER") return <CustomerAccount />;
  return <StaffWorkspace />;
}

function AuthPanel({ redirectTo }: { redirectTo: string }) {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const [registering, setRegistering] = useState(false);
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [verificationSentTo, setVerificationSentTo] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      if (registering) {
        await register({ fullName, email, phone, password });
        setVerificationSentTo(email);
        return;
      } else await login(email, password);
      navigate(redirectTo, { replace: true });
    } catch (requestError) {
      const requestFailure = requestError as { response?: { data?: { message?: string; detail?: string } } };
      const responseData = requestFailure.response?.data;
      const message = responseData?.detail || responseData?.message;
      setError(message || (requestFailure.response
        ? (registering ? "We couldn't create your account. Check the details and try again." : "Email or password wasn't recognised.")
        : "We couldn't reach the account service. Please try again later."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-aside">
        <p className="eyebrow"><span /> YOUR GQIBINDLALA ACCOUNT</p>
        <h1>Good to have<br /><em>you here.</em></h1>
        <p>Keep your details close, place orders with ease, and follow each one from enquiry to event day.</p>
        <div className="auth-aside-mark" aria-hidden="true">G</div>
      </div>
      <div className="auth-form-side">
        {verificationSentTo ? (
          <div className="verification-sent">
            <p className="eyebrow"><span /> CHECK YOUR INBOX</p>
            <h2>One last step.</h2>
            <p>We sent a verification link to <strong>{verificationSentTo}</strong>. Verify your address, then sign in to place orders.</p>
            <button type="button" className="text-button" onClick={() => { setVerificationSentTo(""); setRegistering(false); }}>Back to sign in</button>
          </div>
        ) : <>
        <div className="auth-tabs" role="tablist" aria-label="Account access">
          <button type="button" className={!registering ? "auth-tab selected" : "auth-tab"} onClick={() => { setRegistering(false); setError(""); }}>Sign in</button>
          <button type="button" className={registering ? "auth-tab selected" : "auth-tab"} onClick={() => { setRegistering(true); setError(""); }}>Create account</button>
        </div>
        <div className="auth-intro">
          <h2>{registering ? "Start with the essentials." : "Welcome back."}</h2>
          <p>{registering ? "Your details will be ready whenever you next book." : "Sign in to pick up where you left off."}</p>
        </div>
        <form className="account-form" onSubmit={handleSubmit}>
          {registering && <label className="field-label">Full name<input required autoComplete="name" value={fullName} onChange={(event) => setFullName(event.target.value)} /></label>}
          <label className="field-label">Email<input required type="email" autoComplete="username" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
          {registering && <label className="field-label">Phone number<input required autoComplete="tel" value={phone} onChange={(event) => setPhone(event.target.value)} /></label>}
          <label className="field-label">Password<input required type="password" autoComplete={registering ? "new-password" : "current-password"} minLength={registering ? 10 : undefined} value={password} onChange={(event) => setPassword(event.target.value)} /><span className="field-note">{registering ? "Use at least 10 characters." : ""}</span></label>
          {error && <p className="form-alert" role="alert">{error}</p>}
          <button className="button button-gold auth-submit" type="submit" disabled={submitting}>{submitting ? "Please wait…" : registering ? "Create your account" : "Sign in"}<span aria-hidden="true">↗</span></button>
        </form>
        <p className="auth-security-note">Your account details are protected and used only to manage your orders.</p>
        </>}
      </div>
    </section>
  );
}

function CustomerAccount() {
  const { user } = useAuth();
  const [orders, setOrders] = useState<BookingRecord[]>([]);
  const [references, setReferences] = useState<Record<number, string>>({});
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loadingOrders, setLoadingOrders] = useState(true);
  const [submittingId, setSubmittingId] = useState<number | null>(null);

  async function loadOrders() {
    setLoadingOrders(true);
    try {
      setOrders(await fetchMyBookings());
      setError("");
    } catch {
      setError("Your orders couldn't be loaded. Please try again.");
    } finally {
      setLoadingOrders(false);
    }
  }

  useEffect(() => { void loadOrders(); }, []);

  async function sendReference(event: FormEvent<HTMLFormElement>, orderId: number) {
    event.preventDefault();
    const reference = references[orderId]?.trim();
    if (!reference) return;
    setSubmittingId(orderId);
    setError("");
    try {
      await submitPaymentReference(orderId, reference);
      setMessage(`Payment reference sent for order #${orderId}.`);
      setReferences((previous) => ({ ...previous, [orderId]: "" }));
      await loadOrders();
    } catch {
      setError("We couldn't submit that payment reference. Please try again.");
    } finally {
      setSubmittingId(null);
    }
  }

  return (
    <div className="account-page">
      <section className="account-heading">
        <div><p className="eyebrow"><span /> CUSTOMER ACCOUNT</p><h1>Welcome, <em>{user?.fullName.split(" ")[0]}.</em></h1><p>{user?.personalEmail || user?.loginIdentifier} <span>·</span> {user?.phone}</p></div>
      </section>
      {message && <p className="success-message" role="status">{message}</p>}
      {error && <p className="form-alert" role="alert">{error}</p>}
      <section className="account-orders">
        <div className="account-section-title"><div><span className="step-number">01</span><h2>Your orders</h2></div><span>{orders.length} total</span></div>
        {loadingOrders && <p className="empty-orders">Loading your orders…</p>}
        {!loadingOrders && orders.length === 0 && <div className="empty-orders"><p>No orders yet. Your next gathering starts here.</p><a className="button button-gold" href="/book">Browse and order <span aria-hidden="true">↗</span></a></div>}
        {!loadingOrders && orders.map((order) => (
          <article className="order-card" key={order.id}>
            <header className="order-card-header">
              <div><span className="order-number">ORDER / {String(order.id).padStart(4, "0")}</span><span className="order-date">Placed {new Date(order.createdAt).toLocaleDateString()}</span></div>
              <span className={`status-label status-${order.paymentStatus.toLowerCase()}`}>{order.paymentStatus === "PAID" ? "Payment approved" : order.paymentStatus === "SUBMITTED" ? "Payment review" : order.paymentStatus === "REJECTED" ? "Reference rejected" : order.paymentStatus === "CANCELLED" ? "Order cancelled" : "Awaiting payment"}</span>
            </header>
            <div className="order-lines">
              {order.items.map((item) => <div className="order-line customer-order-line" key={item.id}><span>{item.quantity} × {item.equipmentName}</span><span>{item.startDate} – {item.endDate}</span><strong>{money(item.lineTotal)}</strong><small>{item.quotationIssuedAt ? `Quoted by ${item.quotationIssuedBy ?? item.departmentName}` : "Quotation being prepared"}</small></div>)}
            </div>
            <div className="order-total"><span>Order total</span><strong>{money(order.items.reduce((sum, item) => sum + Number(item.lineTotal), 0))}</strong></div>
            {order.financeRequestStatus !== "NOT_REQUESTED" && <p className="order-note">Finance request: {order.financeRequestStatus === "REQUESTED" ? "sent to the Finance team" : order.financeRequestStatus === "IN_DISCUSSION" ? "being discussed in your order messages" : order.financeRequestStatus === "RESPONDED" ? "Finance has responded. Review the order messages before proceeding to payment." : "declined. You can proceed with full payment below."}</p>}
            {order.paymentStatus === "SUBMITTED" && <p className="order-note">Reference {order.paymentReference} is awaiting review by our payments team.</p>}
            {order.paymentStatus === "PAID" && <p className="order-note">Payment confirmed. Your order is now reserved.</p>}
            {order.paymentStatus === "CANCELLED" && <p className="order-note">This order was cancelled. Contact the business about any payment already made.</p>}
            {(order.paymentStatus === "UNPAID" || order.paymentStatus === "REJECTED") && order.status !== "CANCELLED" && order.items.every((item) => item.quotationIssuedAt) && ["NOT_REQUESTED", "RESPONDED", "DECLINED"].includes(order.financeRequestStatus) && (
              <form className="payment-reference-form" onSubmit={(event) => void sendReference(event, order.id)}>
                <label className="field-label">Payment reference<input required maxLength={255} value={references[order.id] ?? ""} onChange={(event) => setReferences((previous) => ({ ...previous, [order.id]: event.target.value }))} placeholder="Enter the reference from your payment" /></label>
                <button className="button button-gold" type="submit" disabled={submittingId === order.id}>{submittingId === order.id ? "Sending…" : "Submit for review"}<span aria-hidden="true">↗</span></button>
              </form>
            )}
            {(order.paymentStatus === "UNPAID" || order.paymentStatus === "REJECTED") && ["REQUESTED", "IN_DISCUSSION"].includes(order.financeRequestStatus) && <p className="order-note">The Finance team will respond in your order messages before you continue to payment.</p>}
            {(order.paymentStatus === "UNPAID" || order.paymentStatus === "REJECTED") && order.status !== "CANCELLED" && order.items.some((item) => !item.quotationIssuedAt) && <p className="order-note">Payment details will be available after every department completes its quotation.</p>}
            <OrderConversation orderId={order.id} />
          </article>
        ))}
      </section>
    </div>
  );
}

function StaffWorkspace() {
  const { user, logout } = useAuth();
  const paymentsView = user?.role === "PAYMENTS";
  const [orders, setOrders] = useState<BookingRecord[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(true);
  const [error, setError] = useState("");
  const [staffMessage, setStaffMessage] = useState("");

  async function loadOrders() {
    setLoadingOrders(true);
    try {
      if (user?.role === "ADMIN") setOrders([]);
      else setOrders(paymentsView ? await fetchPaymentQueue() : await fetchDepartmentOrders());
      setError("");
    } catch {
      setError("The work queue couldn't be loaded. Please check your access and try again.");
    } finally {
      setLoadingOrders(false);
    }
  }

  useEffect(() => { void loadOrders(); }, [user?.role, user?.departmentId]);

  async function decidePayment(orderId: number, approve: boolean) {
    try {
      await reviewPayment(orderId, approve);
      setStaffMessage(`Order #${orderId} payment ${approve ? "approved" : "rejected"}.`);
      await loadOrders();
    } catch {
      setError("That payment decision couldn't be saved.");
    }
  }

  async function changeFulfillment(itemId: number, status: FulfillmentStatus) {
    try {
      await updateFulfillment(itemId, status);
      setStaffMessage("Order preparation status updated.");
      await loadOrders();
    } catch {
      setError("That preparation update couldn't be saved.");
    }
  }

  async function quoteItem(itemId: number) {
    try {
      await issueQuotation(itemId);
      setStaffMessage("Quotation sent to the customer using the approved price.");
      await loadOrders();
    } catch {
      setError("The quotation could not be issued.");
    }
  }

  return (
    <div className="account-page staff-page">
      <section className="account-heading staff-heading">
        <div><p className="eyebrow"><span /> GQIBINDLALA / STAFF</p><h1>{user?.role === "ADMIN" ? "Supervisor workspace" : paymentsView ? "Finance desk" : <>{user?.departmentName ?? "Department"} <em>orders.</em></>}</h1><p>{user?.fullName} <span>·</span> {user?.role === "ADMIN" ? "Administrator" : user?.role === "PAYMENTS" ? "Finance team" : "Department team"}</p></div>
        <button type="button" className="text-button" onClick={logout}>Sign out</button>
      </section>
      {staffMessage && <p className="success-message" role="status">{staffMessage}</p>}
      {error && <p className="form-alert" role="alert">{error}</p>}

      {user?.role === "ADMIN" && <DepartmentManager />}
      {user?.role === "ADMIN" && <StaffProvisionForm />}
      {user?.role === "ADMIN" && <StaffDirectory />}
      {user?.role === "ADMIN" && <AdminInventoryApprovals />}
      {user?.role === "PAYMENTS" && <FinanceRequestQueue />}
      {user?.role === "DEPARTMENT" && user.departmentId !== null && <DepartmentInventoryWorkspace departmentId={user.departmentId} />}

      {user?.role !== "ADMIN" && <section className="account-orders">
        <div className="account-section-title"><div><span className="step-number">01</span><h2>{paymentsView ? "Awaiting payment review" : "Confirmed orders"}</h2></div><span>{orders.length} in queue</span></div>
        {loadingOrders && <p className="empty-orders">Loading the work queue…</p>}
        {!loadingOrders && orders.length === 0 && <p className="empty-orders">Nothing needs attention right now.</p>}
        {!loadingOrders && orders.map((order) => (
          <article className="order-card staff-order-card" key={order.id}>
            <header className="order-card-header">
              <div><span className="order-number">ORDER / {String(order.id).padStart(4, "0")}</span><span className="order-date">{order.customerName} · {order.customerContact}</span></div>
              <span className={`status-label status-${order.paymentStatus.toLowerCase()}`}>{order.paymentStatus === "SUBMITTED" ? "Payment submitted" : "Paid and confirmed"}</span>
            </header>
            {paymentsView && <p className="staff-reference">Customer payment reference: <strong>{order.paymentReference || "Not supplied"}</strong></p>}
            <div className="order-lines">
              {order.items.map((item) => (
                <div className="staff-order-line" key={item.id}>
                  <div className="order-line"><span>{item.quantity} × {item.equipmentName}</span><span>{item.startDate} – {item.endDate}</span><strong>{money(item.lineTotal)}</strong></div>
                  {!paymentsView && order.status === "PENDING" && <div className="quotation-row"><span>{item.quotationIssuedAt ? `Quoted ${new Date(item.quotationIssuedAt).toLocaleString()}` : "Awaiting quotation"}</span>{!item.quotationIssuedAt && <button className="text-button" type="button" onClick={() => void quoteItem(item.id)}>Issue quotation</button>}</div>}
                  {!paymentsView && order.status === "CONFIRMED" && <label className="fulfillment-field">Preparation status<select value={item.fulfillmentStatus} onChange={(event) => void changeFulfillment(item.id, event.target.value as FulfillmentStatus)}>{fulfillmentOptions.map((option) => <option value={option.value} key={option.value}>{option.label}</option>)}</select></label>}
                </div>
              ))}
            </div>
            {paymentsView && <div className="review-actions"><button className="button button-gold" type="button" onClick={() => void decidePayment(order.id, true)}>Approve payment <span aria-hidden="true">✓</span></button><button className="button button-outline" type="button" onClick={() => void decidePayment(order.id, false)}>Reject reference</button></div>}
            <OrderConversation orderId={order.id} />
          </article>
        ))}
      </section>
      }
    </div>
  );
}

function StaffProvisionForm() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [departmentId, setDepartmentId] = useState("");
  const [fullName, setFullName] = useState("");
  const [personalEmail, setPersonalEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [temporaryPassword, setTemporaryPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchDepartments(true).then((loaded) => {
      setDepartments(loaded.filter((department) => department.active));
      const defaultDepartment = loaded.find((department) => department.active && department.slug !== "finance");
      if (defaultDepartment) setDepartmentId(String(defaultDepartment.id));
    }).catch(() => setError("Departments could not be loaded."));
  }, []);

  useEffect(() => {
    if (!departments.some((department) => String(department.id) === departmentId)) {
      const defaultDepartment = departments.find((department) => department.slug !== "finance") ?? departments[0];
      setDepartmentId(String(defaultDepartment?.id ?? ""));
    }
  }, [departmentId, departments]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const department = departments.find((entry) => entry.id === Number(departmentId));
      const created = await createStaffAccount({
        fullName,
        personalEmail: personalEmail.trim() || null,
        phone,
        temporaryPassword,
        role: department?.slug === "finance" ? "PAYMENTS" : "DEPARTMENT",
        departmentId: Number(departmentId),
      });
      setMessage(`Site login created: ${created.loginIdentifier}. No email was sent.`);
      setFullName("");
      setPersonalEmail("");
      setPhone("");
      setTemporaryPassword("");
    } catch {
      setError("The staff account couldn't be created. Check the login name and department.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="staff-provision">
      <div className="account-section-title"><div><span className="step-number">03</span><h2>Create a staff account</h2></div></div>
      <p className="staff-form-intro">Access follows the assigned department. Staff sign in with a generated site login; no work email is sent.</p>
      <form className="staff-create-form" onSubmit={(event) => void handleSubmit(event)}>
        <label className="field-label">Full name<input required value={fullName} onChange={(event) => setFullName(event.target.value)} /></label>
        <label className="field-label">Personal email (optional)<input type="email" value={personalEmail} onChange={(event) => setPersonalEmail(event.target.value)} /></label>
        <label className="field-label">Phone<input required value={phone} onChange={(event) => setPhone(event.target.value)} /></label>
        <label className="field-label">Initial password<input required type="password" minLength={10} value={temporaryPassword} onChange={(event) => setTemporaryPassword(event.target.value)} /></label>
        <label className="field-label">Department<select required value={departmentId} onChange={(event) => setDepartmentId(event.target.value)}>{departments.map((department) => <option value={department.id} key={department.id}>{department.name}</option>)}</select></label>
        <button className="button button-gold" type="submit" disabled={saving || !departmentId}>{saving ? "Creating…" : "Create staff account"}</button>
      </form>
      {message && <p className="success-message" role="status">{message}</p>}
      {error && <p className="form-alert" role="alert">{error}</p>}
    </section>
  );
}

function DepartmentManager() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [name, setName] = useState("");
  const [loginDomain, setLoginDomain] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  async function loadDepartments() {
    try {
      setDepartments(await fetchDepartments(true));
      setError("");
    } catch {
      setError("Departments could not be loaded.");
    }
  }

  useEffect(() => { void loadDepartments(); }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const created = await createDepartment({ name, loginDomain });
      setMessage(`${created.name} is ready for equipment and staff assignments.`);
      setName("");
      setLoginDomain("");
      await loadDepartments();
    } catch {
      setError("Department could not be created. The name or login domain may already be in use.");
    } finally {
      setSaving(false);
    }
  }

  async function toggleActive(department: Department) {
    try {
      await setDepartmentActive(department.id, !department.active);
      await loadDepartments();
    } catch {
      setError("Department status could not be changed.");
    }
  }

  return (
    <section className="staff-provision department-manager">
      <div className="account-section-title"><div><span className="step-number">02</span><h2>Departments</h2></div><span>{departments.length} total</span></div>
      <p className="staff-form-intro">Departments define equipment ownership and staff login domains. New departments appear in the public catalogue filters.</p>
      <form className="staff-create-form department-create-form" onSubmit={(event) => void handleSubmit(event)}>
        <label className="field-label">Department name<input required maxLength={100} value={name} onChange={(event) => setName(event.target.value)} placeholder="e.g. Sound and lighting" /></label>
        <label className="field-label">Staff login domain<input required maxLength={255} value={loginDomain} onChange={(event) => setLoginDomain(event.target.value)} placeholder="e.g. sound-gqi.co.za" /></label>
        <button className="button button-gold" type="submit" disabled={saving}>{saving ? "Adding…" : "Add department"}</button>
      </form>
      {message && <p className="success-message" role="status">{message}</p>}
      {error && <p className="form-alert" role="alert">{error}</p>}
      <div className="department-list">
        {departments.map((department) => (
          <div className="department-row" key={department.id}>
            <div><strong>{department.name}</strong><span>{department.loginDomain}</span></div>
            <span className={department.active ? "department-active" : "department-inactive"}>{department.active ? "Active" : "Inactive"}</span>
            <button type="button" className="text-button" onClick={() => void toggleActive(department)}>{department.active ? "Deactivate" : "Activate"}</button>
          </div>
        ))}
      </div>
    </section>
  );
}