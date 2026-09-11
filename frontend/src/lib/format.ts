export const currencyFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

export const dateFormatter = new Intl.DateTimeFormat("vi-VN", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
});

export function formatDate(value?: string | null) {
  if (!value) {
    return "Chua cap nhat";
  }
  return dateFormatter.format(new Date(value));
}

export function formatCurrency(value?: number | null, currency?: string | null) {
  if (value == null) {
    return "Chua cap nhat";
  }
  if (currency && currency !== "VND") {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency,
      maximumFractionDigits: 2,
    }).format(value);
  }
  return currencyFormatter.format(value);
}
