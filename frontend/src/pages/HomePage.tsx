import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchEquipment } from "../api/client";
import { Equipment } from "../types/equipment";

export default function HomePage() {
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchEquipment()
      .then(setEquipment)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-900">
        Gqibindlala Catering &amp; Tent Hiring
      </h1>
      <p className="text-gray-600 mt-1 mb-6">
        Chairs, tables, tents, and catering equipment for hire.
      </p>

      {loading && <p className="text-gray-500">Loading equipment...</p>}

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
        {equipment.map((item) => (
          <div key={item.id} className="border rounded-lg p-4 bg-white shadow-sm">
            <h2 className="font-semibold text-gray-900">{item.name}</h2>
            <p className="text-sm text-gray-500 mt-1">{item.description}</p>
            <p className="text-accent font-medium mt-2">
              R{item.pricePerUnitPerDay} / unit / day
            </p>
          </div>
        ))}
      </div>

      <Link
        to="/book"
        className="inline-block mt-8 bg-accent text-white px-5 py-2 rounded-md font-medium hover:opacity-90"
      >
        Start a Booking
      </Link>
    </div>
  );
}
