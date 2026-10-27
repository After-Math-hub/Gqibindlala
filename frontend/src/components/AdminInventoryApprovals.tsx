import { useEffect, useState } from "react";
import { fetchInventoryChanges, reviewInventoryChange } from "../api/client";
import { InventoryChangeRecord } from "../types/equipment";
import InventoryChangeDiscussion from "./InventoryChangeDiscussion";

function changeLabel(request: InventoryChangeRecord) {
  if (request.changeType === "ADD_EQUIPMENT") return `Add ${request.proposedName}`;
  if (request.changeType === "PRICE_CHANGE") return `Price update: ${request.equipmentName}`;
  return `Stock update: ${request.equipmentName}`;
}

export default function AdminInventoryApprovals() {
  const [requests, setRequests] = useState<InventoryChangeRecord[]>([]);
  const [notes, setNotes] = useState<Record<number, string>>({});
  const [error, setError] = useState("");
  const [savingId, setSavingId] = useState<number | null>(null);

  async function load() {
    try {
      setRequests(await fetchInventoryChanges(true));
      setError("");
    } catch {
      setError("Inventory requests could not be loaded.");
    }
  }

  useEffect(() => { void load(); }, []);

  async function decide(requestId: number, approve: boolean) {
    setSavingId(requestId);
    setError("");
    try {
      await reviewInventoryChange(requestId, approve, notes[requestId] ?? "");
      setRequests((current) => current.filter((request) => request.id !== requestId));
    } catch {
      setError("The review could not be saved. Refresh and try again.");
    } finally {
      setSavingId(null);
    }
  }

  return (
    <section className="staff-provision approval-queue">
      <div className="account-section-title"><div><span className="step-number">06</span><h2>Inventory approvals</h2></div><span>{requests.length} awaiting review</span></div>
      <p className="staff-form-intro">Department proposals remain out of the public catalogue until approved.</p>
      {error && <p className="form-alert" role="alert">{error}</p>}
      {requests.length === 0 && !error && <p className="empty-orders">No inventory changes are waiting for approval.</p>}
      <div className="approval-list">
        {requests.map((request) => (
          <article className="approval-card" key={request.id}>
            <div className="approval-card-heading">
              {request.proposedImageUrl && <img src={request.proposedImageUrl} alt="Proposed equipment" />}
              <div><p className="eyebrow"><span /> {request.departmentName}</p><h3>{changeLabel(request)}</h3><span>Requested by {request.requestedBy} · {new Date(request.createdAt).toLocaleString()}</span></div>
            </div>
            <div className="approval-values">
              {request.proposedQuantity !== null && <span>Quantity <strong>{request.proposedQuantity}</strong></span>}
              {request.proposedPrice !== null && <span>Daily rate <strong>R{Number(request.proposedPrice).toFixed(2)}</strong></span>}
            </div>
            <p className="approval-reason">{request.reason}</p>
            <InventoryChangeDiscussion requestId={request.id} />
            <label className="field-label">Review note for department<textarea rows={2} maxLength={1000} value={notes[request.id] ?? ""} onChange={(event) => setNotes((previous) => ({ ...previous, [request.id]: event.target.value }))} placeholder="Optional feedback or reason for rejection" /></label>
            <div className="review-actions">
              <button className="button button-gold" type="button" disabled={savingId === request.id} onClick={() => void decide(request.id, true)}>Approve request <span aria-hidden="true">✓</span></button>
              <button className="button button-outline" type="button" disabled={savingId === request.id} onClick={() => void decide(request.id, false)}>Reject</button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
