import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { checkAvailability, fetchEquipment, submitBooking } from "../api/client";
import { useAuth } from "../components/AuthProvider";
import { Equipment, BookingItemDraft } from "../types/equipment";

type AvailabilityResult = { remaining: number; checked: boolean; error?: string };

function todayAsISODate() {
  const today = new Date();
  const localToday = new Date(today.getTime() - today.getTimezoneOffset() * 60_000);
  return localToday.toISOString().slice(0, 10);
}

function daysInclusive(startDate: string, endDate: string) {
  const start = Date.parse(`${startDate}T00:00:00Z`);
  const end = Date.parse(`${endDate}T00:00:00Z`);
  return Math.max(1, Math.round((end - start) / 86_400_000) + 1);
}

export default function BookingPage() {
  const { user } = useAuth();
  const [catalogue, setCatalogue] = useState<Equipment[]>([]);
  const [items, setItems] = useState<Record<number, BookingItemDraft>>({});
  const [availability, setAvailability] = useState<Record<number, AvailabilityResult>>({});
  const [catalogueError, setCatalogueError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [checkingId, setCheckingId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const today = useMemo(todayAsISODate, []);

  useEffect(() => {
    fetchEquipment()
      .then(setCatalogue)
      .catch(() => setCatalogueError("We couldn't load the equipment list. Please try again later."));
  }, []);

  function updateItem(equipment: Equipment, patch: Partial<BookingItemDraft>) {
    setAvailability((previous) => {
      const next = { ...previous };
      delete next[equipment.id];
      return next;
    });
    setItems((previous) => {
      const existing = previous[equipment.id] ?? {
        equipmentId: equipment.id,
        equipmentName: equipment.name,
        quantity: 0,
        startDate: "",
        endDate: "",
      };
      return { ...previous, [equipment.id]: { ...existing, ...patch } };
    });
  }

  function removeItem(equipmentId: number) {
    setItems((previous) => {
      const next = { ...previous };
      delete next[equipmentId];
      return next;
    });
    setAvailability((previous) => {
      const next = { ...previous };
      delete next[equipmentId];
      return next;
    });
  }

  const selectedItems = Object.values(items).filter(
    (item) => {
      const equipment = catalogue.find((entry) => entry.id === item.equipmentId);
      return item.quantity > 0 && equipment && item.quantity <= equipment.totalQuantity
        && item.startDate && item.endDate && item.startDate >= today && item.endDate >= item.startDate;
    }
  );
  const estimate = selectedItems.reduce((sum, item) => {
    const equipment = catalogue.find((entry) => entry.id === item.equipmentId);
    return sum + (equipment ? Number(equipment.pricePerUnitPerDay) * item.quantity * daysInclusive(item.startDate, item.endDate) : 0);
  }, 0);

  async function handleAvailabilityCheck(equipment: Equipment, item: BookingItemDraft) {
    setCheckingId(equipment.id);
    setAvailability((previous) => ({ ...previous, [equipment.id]: { remaining: 0, checked: false } }));
    try {
      const result = await checkAvailability(equipment.id, item.startDate, item.endDate);
      setAvailability((previous) => ({ ...previous, [equipment.id]: { remaining: result.remaining, checked: true } }));
    } catch {
      setAvailability((previous) => ({ ...previous, [equipment.id]: { remaining: 0, checked: false, error: "Availability could not be checked. You can still send an enquiry." } }));
    } finally {
      setCheckingId(null);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitError("");
    setSubmitting(true);
    try {
      await submitBooking(selectedItems);
      setSubmitted(true);
    } catch {
      setSubmitError("Your enquiry could not be sent. Please check the details and try again.");
    } finally {
      setSubmitting(false);
    }
  }

  if (submitted) {
    return (
      <section className="booking-success">
        <span className="success-mark" aria-hidden="true">✓</span>
        <p className="eyebrow"><span /> ENQUIRY RECEIVED</p>
        <h1>Thank you, {user?.fullName.split(" ")[0]}.</h1>
        <p>Your order is with our team. It will be reserved once payment has been reviewed and approved.</p>
        <Link to="/" className="button button-gold">Back to the collection <span aria-hidden="true">↗</span></Link>
      </section>
    );
  }

  if (!user || user.role !== "CUSTOMER") {
    return (
      <section className="booking-success booking-sign-in">
        <span className="success-mark" aria-hidden="true">G</span>
        <p className="eyebrow"><span /> A MORE PERSONAL WAY TO BOOK</p>
        <h1>Make it yours.</h1>
        <p>Sign in or create a customer account to place an order. Your name and contact details are saved for next time.</p>
        <Link to={`/account?return=${encodeURIComponent("/book")}`} className="button button-gold">Sign in or register <span aria-hidden="true">↗</span></Link>
      </section>
    );
  }

  return (
    <div className="booking-page">
      <section className="booking-heading">
        <p className="eyebrow"><span /> YOUR OCCASION, YOUR WAY</p>
        <h1>Let’s plan <em>something lovely.</em></h1>
        <p>Choose what you need and the dates you have in mind. We’ll confirm availability and get back to you personally.</p>
      </section>

      <form className="booking-form" onSubmit={handleSubmit}>
        <section className="form-section">
          <div className="form-section-heading">
            <span className="step-number">01</span>
            <div><h2>Your account</h2><p>We’ll use the details saved to your account.</p></div>
          </div>
          <div className="saved-customer-details">
            <div><span>Name</span><strong>{user.fullName}</strong></div>
            <div><span>Email</span><strong>{user.personalEmail || user.loginIdentifier}</strong></div>
            <div><span>Phone</span><strong>{user.phone}</strong></div>
          </div>
        </section>

        <section className="form-section equipment-form-section">
          <div className="form-section-heading">
            <span className="step-number">02</span>
            <div><h2>What would you like to hire?</h2><p>Each item can have its own dates and quantity.</p></div>
          </div>

          {catalogueError && <p className="form-alert" role="alert">{catalogueError}</p>}
          {!catalogueError && catalogue.length === 0 && <p className="form-hint">Loading the equipment list…</p>}

          <div className="booking-equipment-list">
            {catalogue.map((equipment, index) => {
              const item = items[equipment.id];
              const checked = availability[equipment.id];
              const rangeValid = Boolean(item?.startDate && item?.endDate && item.startDate >= today && item.endDate >= item.startDate);
              return (
                <article className={`booking-item ${item?.quantity ? "is-selected" : ""}`} key={equipment.id}>
                  <div className="booking-item-top">
                    <span className="booking-item-index">0{index + 1}</span>
                    <div className="booking-item-name"><h3>{equipment.name}</h3><span>R{Number(equipment.pricePerUnitPerDay).toFixed(2)} / unit / day</span></div>
                    <span className="booking-max">{equipment.totalQuantity} in collection</span>
                  </div>
                  <div className="booking-fields">
                    <label className="field-label">Quantity
                      <input type="number" min="0" max={equipment.totalQuantity} step="1" inputMode="numeric" value={item?.quantity ?? 0} onChange={(event) => updateItem(equipment, { quantity: Number(event.target.value) })} />
                    </label>
                    <label className="field-label">Hire from
                      <input type="date" min={today} value={item?.startDate ?? ""} onChange={(event) => updateItem(equipment, { startDate: event.target.value })} />
                    </label>
                    <label className="field-label">Hire until
                      <input type="date" min={item?.startDate || today} value={item?.endDate ?? ""} onChange={(event) => updateItem(equipment, { endDate: event.target.value })} />
                    </label>
                  </div>
                  {item && item.quantity > 0 && (
                    <div className="item-feedback">
                      <div aria-live="polite">
                        {checked?.checked && <span className={checked.remaining >= item.quantity ? "availability-good" : "availability-low"}>{checked.remaining} available for these dates</span>}
                        {checked?.error && <span className="availability-low">{checked.error}</span>}
                        {item.quantity > equipment.totalQuantity && <span className="availability-low">Quantity exceeds the collection size.</span>}
                      </div>
                      <div className="item-actions">
                        <button className="text-button" type="button" disabled={!rangeValid || checkingId === equipment.id} onClick={() => handleAvailabilityCheck(equipment, item)}>
                          {checkingId === equipment.id ? "Checking…" : "Check dates"}
                        </button>
                        <button className="text-button remove-button" type="button" onClick={() => removeItem(equipment.id)}>Remove</button>
                      </div>
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        </section>

        <section className="booking-submit-row">
          <div className="estimate-block">
            <span>Estimated hire total</span>
            <strong>R{estimate.toFixed(2)}</strong>
            <small>Final availability and pricing will be confirmed by our team.</small>
          </div>
          <div className="submit-block">
            {submitError && <p className="form-alert" role="alert">{submitError}</p>}
            <button className="button button-gold submit-button" type="submit" disabled={selectedItems.length === 0 || submitting}>
              {submitting ? "Placing your order…" : "Place order"} <span aria-hidden="true">↗</span>
            </button>
          </div>
        </section>
      </form>
    </div>
  );
}
