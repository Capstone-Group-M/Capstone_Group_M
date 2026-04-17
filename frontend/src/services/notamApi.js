const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export async function fetchNotams({ departure, destination }) {
  const params = new URLSearchParams({ departure, destination });
  const response = await fetch(`${API_BASE_URL}/api/notams?${params.toString()}`);

  if (!response.ok) {
    throw new Error(`Backend request failed with ${response.status}`);
  }

  return response.json();
}
