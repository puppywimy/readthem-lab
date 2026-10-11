export function formatDateTime(dateString?: string | null) {
  if (!dateString) return "";
  return dateString.slice(0, 16).replace("T", " ");
}
