import axios from "axios";
import {
  AuthResponse,
  BookingItemDraft,
  BookingRecord,
  Department,
  Equipment,
  FulfillmentStatus,
  FinanceRequestStatus,
  InventoryChangeRecord,
  InventoryChangeMessage,
  InventoryChangeType,
  OrderMessage,
  UserProfile,
} from "../types/equipment";

const api = axios.create({ baseURL: "/api" });

api.interceptors.request.use((config) => {
  const token = window.localStorage.getItem("gqibindlala-token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export async function registerAccount(details: { fullName: string; email: string; phone: string; password: string }): Promise<{ message: string }> {
  const { data } = await api.post<{ message: string }>("/auth/register", details);
  return data;
}

export async function verifyEmail(token: string): Promise<{ message: string }> {
  const { data } = await api.put<{ message: string }>("/auth/verify-email", { token });
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

export async function fetchInventoryChanges(admin = false): Promise<InventoryChangeRecord[]> {
  const { data } = await api.get<InventoryChangeRecord[]>(admin ? "/inventory-change-requests" : "/inventory-change-requests/mine");
  return data;
}

export async function submitInventoryChange(details: {
  changeType: InventoryChangeType;
  equipmentId?: number;
  name?: string;
  description?: string;
  quantity?: number;
  pricePerUnitPerDay?: number;
  imageUrl?: string;
  reason: string;
}): Promise<InventoryChangeRecord> {
  const { data } = await api.post<InventoryChangeRecord>("/inventory-change-requests", details);
  return data;
}

export async function reviewInventoryChange(id: number, approve: boolean, reviewNote: string) {
  const { data } = await api.post<InventoryChangeRecord>(`/inventory-change-requests/${id}/review`, { approve, reviewNote });
  return data;
}

export async function fetchInventoryChangeMessages(requestId: number): Promise<InventoryChangeMessage[]> {
  const { data } = await api.get<InventoryChangeMessage[]>(`/inventory-change-requests/${requestId}/messages`);
  return data;
}

export async function sendInventoryChangeMessage(requestId: number, content: string): Promise<InventoryChangeMessage> {
  const { data } = await api.post<InventoryChangeMessage>(`/inventory-change-requests/${requestId}/messages`, { content });
  return data;
}

export async function uploadEquipmentImage(file: File): Promise<string> {
  const { data: signature } = await api.post<{
    cloudName: string;
    apiKey: string;
    timestamp: number;
    folder: string;
    signature: string;
  }>("/uploads/cloudinary-signature");
  const payload = new FormData();
  payload.append("file", file);
  payload.append("api_key", signature.apiKey);
  payload.append("timestamp", String(signature.timestamp));
  payload.append("folder", signature.folder);
  payload.append("signature", signature.signature);
  const { data: uploaded } = await axios.post<{ secure_url: string }>(
    `https://api.cloudinary.com/v1_1/${signature.cloudName}/image/upload`,
    payload
  );
  return uploaded.secure_url;
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
  items: BookingItemDraft[],
  paymentOption: "FULL_PAYMENT" | "REQUEST_FINANCE"
) {
  const { data } = await api.post("/bookings", {
    paymentOption,
    items: items.map((i) => ({
      equipmentId: i.equipmentId,
      quantity: i.quantity,
      startDate: i.startDate,
      endDate: i.endDate,
    })),
  });
  return data;
}

export async function fetchFinanceRequests(): Promise<BookingRecord[]> {
  const { data } = await api.get<BookingRecord[]>("/bookings/finance-requests");
  return data;
}

export async function updateFinanceRequestStatus(bookingId: number, status: FinanceRequestStatus) {
  const { data } = await api.patch<BookingRecord>(`/bookings/${bookingId}/finance-request`, { status });
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

export async function issueQuotation(itemId: number): Promise<BookingRecord> {
  const { data } = await api.post<BookingRecord>(`/bookings/items/${itemId}/quotation`);
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

export async function fetchStaffUsers(): Promise<UserProfile[]> {
  const { data } = await api.get<UserProfile[]>("/staff");
  return data;
}

export async function setStaffUserActive(userId: number, active: boolean): Promise<UserProfile> {
  const { data } = await api.patch<UserProfile>(`/staff/${userId}/active`, undefined, { params: { active } });
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
