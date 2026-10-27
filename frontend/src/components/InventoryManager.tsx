import { FormEvent, useEffect, useState } from "react";
import { createEquipment, fetchAdminEquipment, fetchDepartments } from "../api/client";
import { Department, Equipment } from "../types/equipment";

export default function InventoryManager() {
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [totalQuantity, setTotalQuantity] = useState("1");
  const [pricePerUnitPerDay, setPricePerUnitPerDay] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [departmentId, setDepartmentId] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  async function load() {
    try {
      const [items, teams] = await Promise.all([fetchAdminEquipment(), fetchDepartments(true)]);
      setEquipment(items);
      const inventoryDepartments = teams.filter((department) => department.active && department.slug !== "finance");
      setDepartments(inventoryDepartments);
      if (!departmentId && inventoryDepartments.length > 0) setDepartmentId(String(inventoryDepartments[0].id));
      setError("");
    } catch {
      setError("Inventory could not be loaded.");
    }
  }

  useEffect(() => { void load(); }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const created = await createEquipment({
        name,
        description,
        totalQuantity: Number(totalQuantity),
        pricePerUnitPerDay: Number(pricePerUnitPerDay),
        imageUrl,
        departmentId: Number(departmentId),
      });
      setMessage(`${created.name} added to ${created.departmentName}.`);
      setName("");
      setDescription("");
      setTotalQuantity("1");
      setPricePerUnitPerDay("");
      setImageUrl("");
      await load();
    } catch {
      setError("Equipment could not be added. Check the image URL, quantity, price, and department.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="staff-provision inventory-manager">
      <div className="account-section-title"><div><span className="step-number">04</span><h2>Equipment and stock</h2></div><span>{equipment.length} items</span></div>
      <p className="staff-form-intro">Add equipment to a department with its customer-facing photo, quantity, and daily hire price.</p>
      <form className="staff-create-form inventory-create-form" onSubmit={(event) => void handleSubmit(event)}>
        <label className="field-label">Item name<input required maxLength={255} value={name} onChange={(event) => setName(event.target.value)} /></label>
        <label className="field-label">Department<select required value={departmentId} onChange={(event) => setDepartmentId(event.target.value)}>{departments.map((department) => <option value={department.id} key={department.id}>{department.name}</option>)}</select></label>
        <label className="field-label">Total quantity<input required type="number" min="0" step="1" value={totalQuantity} onChange={(event) => setTotalQuantity(event.target.value)} /></label>
        <label className="field-label">Price per unit / day (R)<input required type="number" min="0" step="0.01" value={pricePerUnitPerDay} onChange={(event) => setPricePerUnitPerDay(event.target.value)} /></label>
        <label className="field-label inventory-image-field">Item photo URL<input required type="url" maxLength={500} value={imageUrl} onChange={(event) => setImageUrl(event.target.value)} placeholder="https://..." /><span className="field-note">Paste a hosted photo URL. Cloudinary upload can be connected when its account is configured.</span></label>
        <label className="field-label inventory-description-field">Description<textarea maxLength={1000} value={description} onChange={(event) => setDescription(event.target.value)} rows={2} /></label>
        {imageUrl && <img className="inventory-preview" src={imageUrl} alt="Equipment preview" />}
        <button className="button button-gold" type="submit" disabled={saving || !departmentId}>{saving ? "Adding…" : "Add equipment"}</button>
      </form>
      {message && <p className="success-message" role="status">{message}</p>}
      {error && <p className="form-alert" role="alert">{error}</p>}
      <div className="inventory-list">
        {equipment.map((item) => (
          <article className="inventory-row" key={item.id}>
            {item.imageUrl ? <img src={item.imageUrl} alt="" /> : <span className="inventory-empty-image">No photo</span>}
            <div className="inventory-row-name"><strong>{item.name}</strong><span>{item.departmentName}</span></div>
            <span className="inventory-quantity">{item.totalQuantity} units</span>
            <span className="inventory-price">R{Number(item.pricePerUnitPerDay).toFixed(2)} / day</span>
          </article>
        ))}
      </div>
    </section>
  );
}
