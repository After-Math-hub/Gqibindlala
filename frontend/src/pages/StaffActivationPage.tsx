import { FormEvent, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { activateStaff } from "../api/client";

export default function StaffActivationPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setMessage("");
    if (!token) {
      setError("This activation link is missing its token.");
      return;
    }
    if (password !== confirmPassword) {
      setError("The passwords do not match.");
      return;
    }
    setSaving(true);
    try {
      const response = await activateStaff(token, password);
      setMessage(response.message);
    } catch {
      setError("This activation link is invalid or expired. Ask your administrator to send a new one.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="booking-success email-verification-page">
      <p className="eyebrow"><span /> STAFF ACCOUNT SECURITY</p>
      <h1>Set your password.</h1>
      {message ? (
        <>
          <p role="status">{message}</p>
          <Link to="/account" className="button button-gold">Continue to sign in</Link>
        </>
      ) : (
        <form className="account-form staff-activation-form" onSubmit={(event) => void handleSubmit(event)}>
          <label className="field-label">Password<input type="password" autoComplete="new-password" minLength={10} maxLength={72} required value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          <label className="field-label">Confirm password<input type="password" autoComplete="new-password" minLength={10} maxLength={72} required value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} /></label>
          {error && <p className="form-alert" role="alert">{error}</p>}
          <button className="button button-gold" type="submit" disabled={saving}>{saving ? "Activating…" : "Activate account"}</button>
        </form>
      )}
    </section>
  );
}