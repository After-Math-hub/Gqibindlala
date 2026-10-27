import axios from "axios";
import {
  AuthResponse,
  BookingItemDraft,
  BookingRecord,
  Department,
  Equipment,
  FulfillmentStatus,
  OrderMessage,
  UserProfile,
} from "../types/equipment";

const api = axios.create({ baseURL: "/api" });

api.interceptors.request.use((config) => {
  const token = window.localStorage.getItem("gqibindlala-token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export async function registerAccount(details: { fullName: string; email: string; phone: string; password: string }) {
  const { data } = await api.post<AuthResponse>("/auth/register", details);
  return data;
}

export async function loginAccount(loginIdentifier: string, password: string) {
  const { data } = await api.post<AuthResponse>("/auth/login", { loginIdentifier, password });
  return data;
}

export async function fetchProfile(): Promise<UserProfile> {
  const { data } = await api.get<UserProfile>("/auth/me");
  return data;
}

export async function fetchEquipment(): Promise<Equipment[]> {
  const { data } = await api.get<Equipment[]>("/equipment");
  return data;
}

export async function fetchAdminEquipment(): Promise<Equipment[]> {
  const { data } = await api.get<Equipment[]>("/equipment/admin");
  return data;
}

export async function createEquipment(details: {
  name: string;
  description: string;
  totalQuantity: number;
  pricePerUnitPerDay: number;
  imageUrl: string;
  departmentId: number;
}): Promise<Equipment> {
  const { data } = await api.post<Equipment>("/equipment", details);
  return data;
}

export async function fetchDepartments(admin = false): Promise<Department[]> {
  const { data } = await api.get<Department[]>(admin ? "/departments/admin" : "/departments");
  return data;
}

export async function createDepartment(details: { name: string; loginDomain: string }): Promise<Department> {
  const { data } = await api.post<Department>("/departments", details);
  return data;
}

export async function setDepartmentActive(id: number, active: boolean): Promise<Department> {
  const { data } = await api.patch<Department>(`/departments/${id}/active`, undefined, { params: { active } });
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
  items: BookingItemDraft[]
) {
  const { data } = await api.post("/bookings", {
    items: items.map((i) => ({
      equipmentId: i.equipmentId,
      quantity: i.quantity,
      startDate: i.startDate,
      endDate: i.endDate,
    })),
  });
  return data;
}

export async function fetchMyBookings(): Promise<BookingRecord[]> {
  const { data } = await api.get<BookingRecord[]>("/bookings/mine");
  return data;
}

export async function submitPaymentReference(bookingId: number, reference: string) {
  const { data } = await api.post<BookingRecord>(`/bookings/${bookingId}/payment-reference`, { reference });
  return data;
}

export async function fetchPaymentQueue(): Promise<BookingRecord[]> {
  const { data } = await api.get<BookingRecord[]>("/bookings/payment-review");
  return data;
}

export async function reviewPayment(bookingId: number, approved: boolean) {
  const decision = approved ? "approve" : "reject";
  const { data } = await api.post<BookingRecord>(`/bookings/${bookingId}/payment-review/${decision}`);
  return data;
}

export async function fetchDepartmentOrders(): Promise<BookingRecord[]> {
  const { data } = await api.get<BookingRecord[]>("/bookings/department");
  return data;
}

export async function updateFulfillment(itemId: number, status: FulfillmentStatus) {
  const { data } = await api.patch<BookingRecord>(`/bookings/items/${itemId}/fulfillment`, { status });
  return data;
}

export async function createStaffAccount(details: {
  fullName: string;
  personalEmail: string | null;
  phone: string;
  temporaryPassword: string;
  role: "DEPARTMENT" | "PAYMENTS";
  departmentId: number;
}) {
  const { data } = await api.post<UserProfile>("/staff", details);
  return data;
}

export async function fetchOrderMessages(orderId: number): Promise<OrderMessage[]> {
  const { data } = await api.get<OrderMessage[]>(`/bookings/${orderId}/messages`);
  return data;
}

export async function sendOrderMessage(orderId: number, content: string): Promise<OrderMessage> {
  const { data } = await api.post<OrderMessage>(`/bookings/${orderId}/messages`, { content });
  return data;
}

export default api;
