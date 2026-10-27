import { FormEvent, useEffect, useState } from "react";
import {
  checkAvailability,
  fetchEquipment,
  fetchInventoryChanges,
  submitInventoryChange,
  uploadEquipmentImage,
} from "../api/client";
import { Department, Equipment, InventoryChangeRecord, InventoryChangeType } from "../types/equipment";
import InventoryChangeDiscussion from "./InventoryChangeDiscussion";

function changeLabel(request: InventoryChangeRecord) {
  if (request.changeType === "ADD_EQUIPMENT") return `New item: ${request.proposedName}`;
  if (request.changeType === "PRICE_CHANGE") return `Price change: ${request.equipmentName}`;
  return `Stock change: ${request.equipmentName}`;
}

export default function DepartmentInventoryWorkspace({ departmentId }: { departmentId: number }) {
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [requests, setRequests] = useState<InventoryChangeRecord[]>([]);
  const [changeType, setChangeType] = useState<InventoryChangeType>("ADD_EQUIPMENT");
  const [equipmentId, setEquipmentId] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [quantity, setQuantity] = useState("");
  const [price, setPrice] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [reason, setReason] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [remaining, setRemaining] = useState<Record<number, number>>({});
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);

  async function load() {
    try {
      const [allItems, ownRequests] = await Promise.all([fetchEquipment(), fetchInventoryChanges()]);
      setEquipment(allItems.filter((item) => item.departmentId === departmentId));
      setRequests(ownRequests);
      setError("");
    } catch {
      setError("Department inventory could not be loaded.");
    }
  }

  useEffect(() => { void load(); }, [departmentId]);

  async function checkRemaining() {
    if (!startDate || !endDate || endDate < startDate) {
      setError("Choose a valid date range to check remaining stock.");
      return;
    }
    try {
      const results = await Promise.all(equipment.map(async (item) => [
        item.id,
        (await checkAvailability(item.id, startDate, endDate)).remaining,
      ] as const));
      setRemaining(Object.fromEntries(results));
      setError("");
    } catch {
      setError("Availability could not be checked for those dates.");
    }
  }

  async function selectImage(file?: File) {
    if (!file) return;
    if (!file.type.startsWith("image/") || file.size > 10 * 1024 * 1024) {
      setError("Choose an image file smaller than 10 MB.");
      return;
    }
    setUploading(true);
    setError("");
    try {
      setImageUrl(await uploadEquipmentImage(file));
    } catch {
      setError("Photo upload failed. Check image-service configuration or enter a hosted photo URL.");
    } finally {
      setUploading(false);
    }
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      await submitInventoryChange({
        changeType,
        equipmentId: changeType === "ADD_EQUIPMENT" ? undefined : Number(equipmentId),
        name: changeType === "ADD_EQUIPMENT" ? name : undefined,
        description: changeType === "ADD_EQUIPMENT" ? description : undefined,
        quantity: changeType === "STOCK_CHANGE" || changeType === "ADD_EQUIPMENT" ? Number(quantity) : undefined,
        pricePerUnitPerDay: changeType === "PRICE_CHANGE" || changeType === "ADD_EQUIPMENT" ? Number(price) : undefined,
        imageUrl: changeType === "ADD_EQUIPMENT" ? imageUrl : undefined,
        reason,
      });
      setMessage("Your proposal has been sent to the main admin for approval.");
      setName("");
      setDescription("");
      setQuantity("");
      setPrice("");
      setImageUrl("");
      setReason("");
      await load();
    } catch {
      setError("The proposal could not be submitted. Check the fields and try again.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="department-inventory-workspace">
      <section className="staff-provision department-stock-section">
        <div className="account-section-title"><div><span className="step-number">02</span><h2>Department stock</h2></div><span>{equipment.length} items</span></div>
        <p className="staff-form-intro">Check what remains for a selected hire period. Approved prices and stock stay live until the main admin accepts a proposal.</p>
        <div className="availability-check-row">
          <label className="field-label">From<input type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} /></label>
          <label className="field-label">Until<input type="date" min={startDate || undefined} value={endDate} onChange={(event) => setEndDate(event.target.value)} /></label>
          <button type="button" className="button button-outline" onClick={() => void checkRemaining()}>Check remaining</button>
        </div>
        <div className="inventory-list">
          {equipment.map((item) => (
            <article className="inventory-row" key={item.id}>
              {item.imageUrl ? <img src={item.imageUrl} alt="" /> : <span className="inventory-empty-image">No photo</span>}
              <div className="inventory-row-name"><strong>{item.name}</strong><span>Approved rate · R{Number(item.pricePerUnitPerDay).toFixed(2)} / day</span></div>
              <span className="inventory-quantity">{remaining[item.id] === undefined ? `${item.totalQuantity} total` : `${remaining[item.id]} left`}</span>
            </article>
          ))}
        </div>
      </section>

      <section className="staff-provision inventory-proposal-section">
        <div className="account-section-title"><div><span className="step-number">03</span><h2>Propose an inventory change</h2></div></div>
        <p className="staff-form-intro">New equipment, price changes, and stock changes require admin approval before customers see them.</p>
        <form className="staff-create-form inventory-proposal-form" onSubmit={(event) => void submit(event)}>
          <label className="field-label">Request type<select value={changeType} onChange={(event) => setChangeType(event.target.value as InventoryChangeType)}><option value="ADD_EQUIPMENT">Add new equipment</option><option value="PRICE_CHANGE">Change an approved price</option><option value="STOCK_CHANGE">Change stock quantity</option></select></label>
          {changeType !== "ADD_EQUIPMENT" && <label className="field-label">Equipment<select required value={equipmentId} onChange={(event) => setEquipmentId(event.target.value)}><option value="">Choose an item</option>{equipment.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select></label>}
          {changeType === "ADD_EQUIPMENT" && <label className="field-label">Item name<input required maxLength={255} value={name} onChange={(event) => setName(event.target.value)} /></label>}
          {changeType === "ADD_EQUIPMENT" && <label className="field-label">Description<input maxLength={1000} value={description} onChange={(event) => setDescription(event.target.value)} /></label>}
          {(changeType === "ADD_EQUIPMENT" || changeType === "STOCK_CHANGE") && <label className="field-label">Proposed total quantity<input required type="number" min="0" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} /></label>}
          {(changeType === "ADD_EQUIPMENT" || changeType === "PRICE_CHANGE") && <label className="field-label">Proposed daily rate (R)<input required type="number" min="0" step="0.01" value={price} onChange={(event) => setPrice(event.target.value)} /></label>}
          {changeType === "ADD_EQUIPMENT" && <label className="field-label inventory-image-field">Equipment photo<input type="file" accept="image/*" onChange={(event) => void selectImage(event.target.files?.[0])} /><span className="field-note">{uploading ? "Uploading…" : "Upload an image or paste a hosted image URL below."}</span></label>}
          {changeType === "ADD_EQUIPMENT" && <label className="field-label inventory-image-field">Photo URL<input required type="url" maxLength={500} value={imageUrl} onChange={(event) => setImageUrl(event.target.value)} placeholder="https://..." /></label>}
          <label className="field-label proposal-reason-field">Reason for request<textarea required maxLength={1000} rows={2} value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Explain why this change is needed" /></label>
          {imageUrl && changeType === "ADD_EQUIPMENT" && <img className="inventory-preview" src={imageUrl} alt="Proposed equipment preview" />}
          <button className="button button-gold" type="submit" disabled={saving || uploading || (changeType !== "ADD_EQUIPMENT" && !equipmentId)}>{saving ? "Sending proposal…" : "Send for approval"}</button>
        </form>
        {message && <p className="success-message" role="status">{message}</p>}
        {error && <p className="form-alert" role="alert">{error}</p>}
      </section>

      <section className="staff-provision department-proposal-history">
        <div className="account-section-title"><div><span className="step-number">04</span><h2>My proposals</h2></div><span>{requests.length} total</span></div>
        {requests.length === 0 && <p className="empty-orders">No change requests yet.</p>}
        {requests.map((request) => (
          <article className="proposal-history-row" key={request.id}>
            <div><strong>{changeLabel(request)}</strong><span>{request.reason}</span>{request.reviewNote && <span>Admin note: {request.reviewNote}</span>}</div>
            <span className={`status-label status-${request.status.toLowerCase()}`}>{request.status.toLowerCase()}</span>
            <InventoryChangeDiscussion requestId={request.id} />
          </article>
        ))}
      </section>
    </div>
  );
}
