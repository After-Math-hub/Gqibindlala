import { useEffect, useState } from "react";
import { fetchStaffUsers, setStaffUserActive } from "../api/client";
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
            <span className={user.active ? "department-active" : "department-inactive"}>{user.active ? "Active" : "Inactive"}</span>
            <button type="button" className="text-button" disabled={savingId === user.id} onClick={() => void toggle(user)}>{user.active ? "Deactivate" : "Reactivate"}</button>
          </article>
        ))}
      </div>
    </section>
  );
}
