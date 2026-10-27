import { useEffect, useState } from "react";
import { fetchFinanceRequests, updateFinanceRequestStatus } from "../api/client";
import { BookingRecord, FinanceRequestStatus } from "../types/equipment";
import OrderConversation from "./OrderConversation";

const statusLabels: Record<FinanceRequestStatus, string> = {
  NOT_REQUESTED: "Not requested",
  REQUESTED: "New request",
  IN_DISCUSSION: "In discussion",
  RESPONDED: "Response sent",
  DECLINED: "Declined",
};

function total(order: BookingRecord) {
  return order.items.reduce((sum, item) => sum + Number(item.lineTotal), 0);
}

export default function FinanceRequestQueue() {
  const [requests, setRequests] = useState<BookingRecord[]>([]);
  const [error, setError] = useState("");
  const [savingId, setSavingId] = useState<number | null>(null);

  async function load() {
    try {
      setRequests(await fetchFinanceRequests());
      setError("");
    } catch {
      setError("Finance requests could not be loaded.");
    }
  }

  useEffect(() => { void load(); }, []);

  async function update(orderId: number, status: FinanceRequestStatus) {
    setSavingId(orderId);
    setError("");
    try {
      await updateFinanceRequestStatus(orderId, status);
      await load();
    } catch {
      setError("The Finance request could not be updated.");
    } finally {
      setSavingId(null);
    }
  }

  return (
    <section className="staff-provision finance-request-queue">
      <div className="account-section-title"><div><span className="step-number">02</span><h2>Finance requests</h2></div><span>{requests.length} open</span></div>
      <p className="staff-form-intro">Discuss possible payment arrangements with customers in the order thread. The site does not approve credit or calculate instalments.</p>
      {error && <p className="form-alert" role="alert">{error}</p>}
      {requests.length === 0 && !error && <p className="empty-orders">No Finance requests are waiting.</p>}
      <div className="finance-request-list">
        {requests.map((order) => (
          <article className="finance-request-card" key={order.id}>
            <header className="order-card-header">
              <div><span className="order-number">ORDER / {String(order.id).padStart(4, "0")}</span><span className="order-date">{order.customerName} · {order.customerContact}</span></div>
              <span className="status-label">{statusLabels[order.financeRequestStatus]}</span>
            </header>
            <div className="order-lines">
              {order.items.map((item) => <div className="order-line" key={item.id}><span>{item.quantity} × {item.equipmentName}</span><span>{item.startDate} – {item.endDate}</span><strong>R{Number(item.lineTotal).toFixed(2)}</strong></div>)}
            </div>
            <div className="order-total"><span>Quoted order total</span><strong>R{total(order).toFixed(2)}</strong></div>
            <OrderConversation orderId={order.id} />
            <div className="review-actions finance-actions">
              {order.financeRequestStatus === "REQUESTED" && <button className="button button-outline" disabled={savingId === order.id} type="button" onClick={() => void update(order.id, "IN_DISCUSSION")}>Start discussion</button>}
              {order.financeRequestStatus === "IN_DISCUSSION" && <button className="button button-gold" disabled={savingId === order.id} type="button" onClick={() => void update(order.id, "RESPONDED")}>Response sent</button>}
              {(order.financeRequestStatus === "REQUESTED" || order.financeRequestStatus === "IN_DISCUSSION") && <button className="button button-outline" disabled={savingId === order.id} type="button" onClick={() => void update(order.id, "DECLINED")}>Decline request</button>}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
