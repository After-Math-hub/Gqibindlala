import axios from "axios";
import { Equipment, BookingItemDraft } from "../types/equipment";

const api = axios.create({ baseURL: "/api" });

export async function fetchEquipment(): Promise<Equipment[]> {
  const { data } = await api.get<Equipment[]>("/equipment");
  return data;
}

export async function checkAvailability(
  equipmentId: number,
  startDate: string,
  endDate: string
): Promise<{ remaining: number }> {
  const { data } = await api.get(`/equipment/${equipmentId}/availability`, {
    params: { startDate, endDate },
  });
  return data;
}

export async function submitBooking(
  customerName: string,
  customerContact: string,
  items: BookingItemDraft[]
) {
  const { data } = await api.post("/bookings", {
    customerName,
    customerContact,
    items: items.map((i) => ({
      equipmentId: i.equipmentId,
      quantity: i.quantity,
      startDate: i.startDate,
      endDate: i.endDate,
    })),
  });
  return data;
}

export default api;
