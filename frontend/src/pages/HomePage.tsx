import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { fetchDepartments, fetchEquipment } from "../api/client";
import { Department, Equipment } from "../types/equipment";

export default function HomePage() {
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [activeDepartment, setActiveDepartment] = useState<number | "ALL">("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([fetchEquipment(), fetchDepartments()])
      .then(([items, activeDepartments]) => {
        setEquipment(items);
        setDepartments(activeDepartments);
      })
      .catch(() => setError("We couldn't load the collection. Please try again in a moment."))
      .finally(() => setLoading(false));
  }, []);

  const visibleEquipment = useMemo(
    () => activeDepartment === "ALL"
      ? equipment
      : equipment.filter((item) => item.departmentId === activeDepartment),
    [activeDepartment, equipment]
  );

  return (
    <>
      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow"><span /> MADE FOR THE MOMENTS THAT MATTER</p>
          <h1>Gather beautifully.<br /><em>We’ll bring the rest.</em></h1>
          <p className="hero-description">
            Considered event essentials, thoughtfully prepared for your celebration, your people, and the way you want to remember it.
          </p>
          <div className="hero-actions">
            <Link to="/book" className="button button-gold">Build your enquiry <span aria-hidden="true">↗</span></Link>
            <a href="#collection" className="text-link">Explore the collection <span aria-hidden="true">↓</span></a>
          </div>
          <div className="hero-note">
            <span className="note-rule" />
            <span>Reliable pieces. Personal service.<br />A little more care in every detail.</span>
          </div>
        </div>
        <div className="hero-visual" role="img" aria-label="An elegant event table arranged for a celebration">
          <div className="hero-image-caption"><span>01</span> THE ART OF A GOOD GATHERING</div>
          <div className="hero-photo-credit">EVENTS, MADE MORE MEMORABLE</div>
        </div>
        <div className="hero-index" aria-hidden="true">GQ / 01</div>
      </section>

      <section className="intro-strip" aria-label="Service details">
        <div><span className="strip-number">01</span><span>Made for real occasions</span></div>
        <div><span className="strip-number">02</span><span>Clear, per-day pricing</span></div>
        <div><span className="strip-number">03</span><span>Every item, on your dates</span></div>
      </section>

      <section id="collection" className="collection-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow"><span /> THE HIRE COLLECTION</p>
            <h2>Good things, <em>gathered.</em></h2>
          </div>
          <p className="section-description">The practical pieces behind a lovely day. Browse what’s available, then tell us what you have in mind.</p>
        </div>

        <div className="category-tabs" aria-label="Filter equipment by category">
          {[{ id: "ALL" as const, name: "Everything" }, ...departments].map((department) => (
            <button
              key={department.id}
              type="button"
              className={activeDepartment === department.id ? "category-tab selected" : "category-tab"}
              aria-pressed={activeDepartment === department.id}
              onClick={() => setActiveDepartment(department.id)}
            >
              {department.name}
            </button>
          ))}
        </div>

        {loading && <div className="catalogue-message" role="status">Preparing the collection…</div>}
        {!loading && error && <div className="catalogue-message error-message" role="alert">{error}</div>}
        {!loading && !error && visibleEquipment.length === 0 && (
          <div className="catalogue-message">There are no items in this category just yet.</div>
        )}

        <div className="equipment-grid">
          {visibleEquipment.map((item, index) => (
            <article className="equipment-card" key={item.id}>
              <div className={`equipment-art art-${item.departmentSlug}`}>
                {item.imageUrl ? <img src={item.imageUrl} alt={item.name} loading="lazy" /> : <span className="equipment-number">0{index + 1}</span>}
                <span className="equipment-category">{item.departmentName}</span>
              </div>
              <div className="equipment-details">
                <div className="equipment-title-row">
                  <h3>{item.name}</h3>
                  <span className="equipment-stock">{item.totalQuantity} in collection</span>
                </div>
                <p>{item.description || "A dependable essential for your next gathering."}</p>
                <div className="equipment-bottom">
                  <span className="equipment-price"><strong>R{Number(item.pricePerUnitPerDay).toFixed(2)}</strong> / unit / day</span>
                  <Link to="/book" className="card-arrow" aria-label={`Enquire about ${item.name}`} title={`Enquire about ${item.name}`}>↗</Link>
                </div>
              </div>
            </article>
          ))}
        </div>

        {!loading && !error && equipment.length > 0 && (
          <div className="catalogue-endnote"><span /> Availability is confirmed for your chosen dates when you enquire.</div>
        )}
      </section>

      <section className="closing-band">
        <div className="closing-ornament" aria-hidden="true">✳</div>
        <p className="eyebrow">A GOOD EVENT STARTS SOMEWHERE</p>
        <h2>Let’s make room<br /><em>for a good day.</em></h2>
        <Link to="/book" className="button button-light">Start an enquiry <span aria-hidden="true">↗</span></Link>
      </section>
    </>
  );
}
