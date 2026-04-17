import { useState } from "react";
import AirportForm from "./components/AirportForm";
import "./App.css";
import "./services/firebase";

function App() {
  const [route, setRoute] = useState(null);
  const [notamResponse, setNotamResponse] = useState(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  async function handleFormSubmit({ departure, destination }) {
    setRoute({ departure, destination });
    setError("");
    setIsLoading(true);

    try {
      const data = await fetchNotams({ departure, destination });
      setNotamResponse(data);
    } catch (requestError) {
      setNotamResponse(null);
      setError(requestError.message || "Failed to fetch NOTAMs. Please try again.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <h1>NOTAM Analyzer</h1>
      <AirportForm onSubmit={handleFormSubmit} isSubmitting={isLoading} />

      {route && (
        <p style={{ marginTop: "1.5rem" }}>
          Route: {route.departure} → {route.destination}
        </p>
      )}

      {isLoading && <p>Loading NOTAMs from backend...</p>}
      {error && <p style={{ color: "#d9534f" }}>{error}</p>}

      {notamResponse && !isLoading && (
        <div style={{ marginTop: "1.5rem", textAlign: "left" }}>
          <h2>Backend response received</h2>
          <p>
            Found {notamResponse.count} NOTAM(s) for {notamResponse.departure} →{" "}
            {notamResponse.destination}
          </p>
          <pre style={{ whiteSpace: "pre-wrap", overflowX: "auto" }}>
            {JSON.stringify(notamResponse, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}

export default App;