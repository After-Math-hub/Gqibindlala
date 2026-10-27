export interface Department {
  id: number;
  name: string;
  slug: string;
  loginDomain: string;
  active: boolean;
}

export interface Equipment {
  id: number;
  name: string;
  departmentId: number;
  departmentName: string;
  departmentSlug: string;
  description: string;
  totalQuantity: number;
  pricePerUnitPerDay: number;
  imageUrl?: string;
}

export interface BookingItemDraft {
  equipmentId: number;
  equipmentName: string;
  quantity: number;
  startDate: string; // ISO date, e.g. 2026-10-01
  endDate: string;
}

export type AppRole = "CUSTOMER" | "DEPARTMENT" | "PAYMENTS" | "ADMIN";
export type PaymentStatus = "UNPAID" | "SUBMITTED" | "PAID" | "REJECTED" | "CANCELLED";
export type FinanceRequestStatus = "NOT_REQUESTED" | "REQUESTED" | "IN_DISCUSSION" | "RESPONDED" | "DECLINED";
export type FulfillmentStatus = "NOT_STARTED" | "PREPARING" | "READY" | "FULFILLED";

export interface UserProfile {
  id: number;
  fullName: string;
  loginIdentifier: string;
  personalEmail: string | null;
  phone: string;
  role: AppRole;
  departmentId: number | null;
  departmentName: string | null;
  active: boolean;
}

export interface AuthResponse {
  token: string;
  user: UserProfile;
}

export interface BookingItemRecord {
  id: number;
  equipmentId: number;
  equipmentName: string;
  departmentId: number;
  departmentName: string;
  quantity: number;
  startDate: string;
  endDate: string;
  unitPrice: number;
  lineTotal: number;
  fulfillmentStatus: FulfillmentStatus;
  quotationIssuedAt: string | null;
  quotationIssuedBy: string | null;
}

export type InventoryChangeType = "ADD_EQUIPMENT" | "PRICE_CHANGE" | "STOCK_CHANGE";
export type InventoryChangeStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface InventoryChangeRecord {
  id: number;
  changeType: InventoryChangeType;
  status: InventoryChangeStatus;
  departmentId: number;
  departmentName: string;
  equipmentId: number | null;
  equipmentName: string | null;
  proposedName: string | null;
  proposedQuantity: number | null;
  proposedPrice: number | null;
  proposedImageUrl: string | null;
  reason: string;
  requestedBy: string;
  reviewedBy: string | null;
  reviewNote: string | null;
  createdAt: string;
  reviewedAt: string | null;
}

export interface InventoryChangeMessage {
  id: number;
  changeRequestId: number;
  senderName: string;
  senderRole: AppRole;
  content: string;
  createdAt: string;
}

export interface OrderMessage {
  id: number;
  bookingId: number;
  senderName: string;
  senderRole: AppRole;
  departmentId: number | null;
  departmentName: string | null;
  content: string;
  createdAt: string;
}

export interface BookingRecord {
  id: number;
  customerName: string;
  customerContact: string;
  status: "PENDING" | "CONFIRMED" | "CANCELLED";
  paymentStatus: PaymentStatus;
  financeRequestStatus: FinanceRequestStatus;
  paymentReference: string | null;
  createdAt: string;
  items: BookingItemRecord[];
}
