import { useEffect, useState } from "react";
import { fetchEquipment, submitBooking } from "../api/client";
import { Equipment, BookingItemDraft } from "../types/equipment";

export default function BookingPage() {
  const [catalogue, setCatalogue] = useState<Equipment[]>([]);
  const [items, setItems] = useState<Record<number, BookingItemDraft>>({});
  const [customerName, setCustomerName] = useState("");
  const [customerContact, setCustomerContact] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    fetchEquipment().then(setCatalogue);
  }, []);

  function updateItem(equipment: Equipment, patch: Partial<BookingItemDraft>) {
    setItems((prev) => {
      const existing = prev[equipment.id] ?? {
        equipmentId: equipment.id,
        equipmentName: equipment.name,
        quantity: 1,
        startDate: "",
        endDate: "",
      };
      return { ...prev, [equipment.id]: { ...existing, ...patch } };
    });
  }

  function removeItem(equipmentId: number) {
    setItems((prev) => {
      const next = { ...prev };
      delete next[equipmentId];
      return next;
    });
  }

  const selectedItems = Object.values(items).filter(
    (i) => i.quantity > 0 && i.startDate && i.endDate
  );

  async function handleSubmit() {
    setSubmitting(true);
    try {
      await submitBooking(customerName, customerContact, selectedItems);
      setSubmitted(true);
    } finally {
      setSubmitting(false);
    }
  }

  if (submitted) {
    return (
      <div className="max-w-xl mx-auto p-6 text-center">
        <h1 className="text-xl font-bold text-gray-900">Booking Request Sent</h1>
        <p className="text-gray-600 mt-2">
          We'll be in touch to confirm availability and arrange payment.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-6">
      <h1 className="text-xl font-bold text-gray-900 mb-4">Request a Booking</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
        <input
          className="border rounded-md p-2"
          placeholder="Your name"
          value={customerName}
          onChange={(e) => setCustomerName(e.target.value)}
        />
        <input
          className="border rounded-md p-2"
          placeholder="Phone or email"
          value={customerContact}
          onChange={(e) => setCustomerContact(e.target.value)}
        />
      </div>

      <div className="space-y-4">
        {catalogue.map((item) => {
          const draft = items[item.id];
          return (
            <div key={item.id} className="border rounded-lg p-4 bg-white">
              <div className="flex items-center justify-between">
                <h3 className="font-semibold text-gray-900">{item.name}</h3>
                <span className="text-sm text-gray-500">
                  R{item.pricePerUnitPerDay} / unit / day
                </span>
              </div>

              {/* Each item has its own quantity + date range — booking items are independent */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mt-3">
                <label className="text-sm text-gray-600">
                  Quantity
                  <input
                    type="number"
                    min={0}
                    max={item.totalQuantity}
                    className="border rounded-md p-2 w-full mt-1"
                    value={draft?.quantity ?? 0}
                    onChange={(e) =>
                      updateItem(item, { quantity: Number(e.target.value) })
                    }
                  />
                </label>
                <label className="text-sm text-gray-600">
                  Start date
                  <input
                    type="date"
                    className="border rounded-md p-2 w-full mt-1"
                    value={draft?.startDate ?? ""}
                    onChange={(e) => updateItem(item, { startDate: e.target.value })}
                  />
                </label>
                <label className="text-sm text-gray-600">
                  End date
                  <input
                    type="date"
                    className="border rounded-md p-2 w-full mt-1"
                    value={draft?.endDate ?? ""}
                    onChange={(e) => updateItem(item, { endDate: e.target.value })}
                  />
                </label>
              </div>

              {draft && draft.quantity > 0 && (
                <button
                  className="text-sm text-gray-400 mt-2 hover:text-gray-600"
                  onClick={() => removeItem(item.id)}
                >
                  Remove from booking
                </button>
              )}
            </div>
          );
        })}
      </div>

      <button
        className="mt-6 bg-accent text-white px-5 py-2 rounded-md font-medium disabled:opacity-50"
        disabled={selectedItems.length === 0 || !customerName || !customerContact || submitting}
        onClick={handleSubmit}
      >
        {submitting ? "Submitting..." : "Submit Booking Request"}
      </button>
    </div>
  );
}
