import { useEffect, useState } from "react";
import { fetchStaffUsers, resendStaffActivation, setStaffUserActive } from "../api/client";
import { UserProfile } from "../types/equipment";

export default function StaffDirectory() {
  const [staff, setStaff] = useState<UserProfile[]>([]);
  const [error, setError] = useState("");
  const [savingId, setSavingId] = useState<number | null>(null);

  async function load() {
    try {
      setStaff(await fetchStaffUsers());
      setError("");
    } catch {
      setError("Staff accounts could not be loaded.");
    }
  }

  useEffect(() => { void load(); }, []);

  async function toggle(user: UserProfile) {
    setSavingId(user.id);
    setError("");
    try {
      const updated = await setStaffUserActive(user.id, !user.active);
      setStaff((previous) => previous.map((entry) => entry.id === updated.id ? updated : entry));
    } catch {
      setError("Staff access could not be changed.");
    } finally {
      setSavingId(null);
    }
  }

  async function resendActivation(user: UserProfile) {
    setSavingId(user.id);
    setError("");
    try {
      await resendStaffActivation(user.id);
    } catch {
      setError("The activation email could not be sent.");
    } finally {
      setSavingId(null);
    }
  }

  return (
    <section className="staff-provision staff-directory">
      <div className="account-section-title"><div><span className="step-number">04</span><h2>Staff access</h2></div><span>{staff.length} accounts</span></div>
      <p className="staff-form-intro">Deactivate access when a staff member leaves. Their current sessions are revoked immediately.</p>
      {error && <p className="form-alert" role="alert">{error}</p>}
      {staff.length === 0 && !error && <p className="empty-orders">No staff accounts yet.</p>}
      <div className="staff-directory-list">
        {staff.map((user) => (
          <article className="staff-directory-row" key={user.id}>
            <div className="staff-directory-identity"><strong>{user.fullName}</strong><span>{user.loginIdentifier}</span></div>
            <div className="staff-directory-department"><strong>{user.departmentName ?? "Finance"}</strong><span>{user.role === "PAYMENTS" ? "Payment review" : "Department"}</span></div>
            <span className={user.staffActivated ? (user.active ? "department-active" : "department-inactive") : "department-inactive"}>{user.staffActivated ? (user.active ? "Active" : "Inactive") : "Pending activation"}</span>
            {user.staffActivated
              ? <button type="button" className="text-button" disabled={savingId === user.id} onClick={() => void toggle(user)}>{user.active ? "Deactivate" : "Reactivate"}</button>
              : <button type="button" className="text-button" disabled={savingId === user.id} onClick={() => void resendActivation(user)}>{savingId === user.id ? "Sending…" : "Resend activation"}</button>}
          </article>
        ))}
      </div>
    </section>
  );
}
