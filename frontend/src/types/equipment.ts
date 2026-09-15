export type EquipmentCategory =
  | "CHAIR"
  | "TABLE"
  | "TENT"
  | "MOBILE_FRIDGE"
  | "MEAT_CUTTER";

export interface Equipment {
  id: number;
  name: string;
  category: EquipmentCategory;
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
