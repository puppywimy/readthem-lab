export function formatDate(dateString?: string | null) {
  if (!dateString) return "";
  return dateString.slice(0, 10).replaceAll("-", ".");
}

export function timeAgo(dateString?: string | null) {
  if (!dateString) return "";

  const minutes = Math.floor(
    (Date.now() - new Date(dateString).getTime()) / 60000,
  );
  if (Number.isNaN(minutes)) return "";
  if (minutes < 1) return "방금 전";
  if (minutes < 60) return `${minutes}분 전`;
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)}시간 전`;
  if (minutes < 60 * 48) return "어제";
  return formatDate(dateString);
}
