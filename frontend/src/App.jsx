import { useState } from "react";
import AirportForm from "./components/AirportForm";
import NotamList from "./components/NotamList";
import { fetchNotams } from "./services/notamApi";

function App() {
  const [notams, setNotams] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit({ departure, destination }) {
    setLoading(true);
    setError("");

    try {
      const data = await fetchNotams({ departure, destination });
      setNotams(data.notams || data);
    } catch (err) {
      setError("Failed to fetch NOTAMs");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <h1>NOTAM Analyzer</h1>

      <AirportForm onSubmit={handleSubmit} isSubmitting={loading} />

      {loading && <p>Loading...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      <NotamList notams={notams} />
    </div>
  );
}

export default App;