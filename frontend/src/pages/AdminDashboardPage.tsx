export default function AdminDashboardPage() {
  // Phase 4: list bookings from GET /api/bookings, confirm/cancel via
  // POST /api/bookings/{id}/confirm and /cancel, manage equipment CRUD.
  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-xl font-bold text-gray-900">Admin Dashboard</h1>
      <p className="text-gray-500 mt-2">
        Bookings, inventory, and payment confirmation go here — built in Phase 4.
      </p>
    </div>
  );
}
