// src/lib/api.js
const API_URL = process.env.REACT_APP_API_URL ?? "http://localhost:8080";

async function fetchJson(path, { method = "GET", body, headers = {}, ...rest } = {}) {
  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers: { "Content-Type": "application/json", ...headers },
    body: body ? JSON.stringify(body) : undefined,
    ...rest,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status}: ${text || res.statusText}`);
  }
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res.text();
}

async function fetchMultipart(path, formData, options = {}) {
  const res = await fetch(`${API_URL}${path}`, {
    method: "POST",
    body: formData,
    ...options, // nezasahujeme do headers, aby se neposílal content-type ručně
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${await res.text()}`);
  return res.json().catch(() => ({}));
}

export { API_URL, fetchJson, fetchMultipart };
