const API_URL = import.meta.env.VITE_API_URL || "/api/v1";
const KEY = "ccid_session";

export class ApiError extends Error {
  constructor(status, title, detail, fieldErrors = {}) {
    super(detail || title);
    this.status = status;
    this.title = title;
    this.fieldErrors = fieldErrors;
  }
}

export function getSession() {
  try { return JSON.parse(sessionStorage.getItem(KEY)); } catch { return null; }
}
export function saveSession(session) {
  sessionStorage.setItem(KEY, JSON.stringify(session));
}
export function clearSession() {
  sessionStorage.removeItem(KEY);
}

function expire() {
  clearSession();
  window.dispatchEvent(new Event("ccid:logout"));
}

export async function api(path, { method = "GET", body, auth = true } = {}) {
  const headers = {};
  if (body) headers["Content-Type"] = "application/json";

  if (auth) {
    const session = getSession();
    if (session) {
      if (new Date(session.expiresAt) <= new Date()) {
        expire();
        throw new ApiError(401, "Session expired", "Your session expired. Please log in again.");
      }
      headers.Authorization = `Bearer ${session.accessToken}`;
    }
  }

  let res;
  try {
    res = await fetch(`${API_URL}${path}`, {
      method, headers, body: body ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, "Network error",
      "Can't reach the server. It may be waking up, so please try again in a moment.");
  }

  const data = await res.json().catch(() => null);
  if (res.ok) return data;
  if (res.status === 401 && auth) expire();
  throw new ApiError(res.status, data?.title ?? "Error",
    data?.detail ?? "Something went wrong.", data?.errors ?? {});
}
