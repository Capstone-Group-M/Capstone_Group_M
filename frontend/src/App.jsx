import { useState } from "react";
import AirportForm from "./components/AirportForm";
import NotamList from "./components/NotamList";
import "./App.css";
import "./services/firebase";

function App() {
  const [route, setRoute] = useState(null);
  const [notams, setNotams] = useState([]);

  function handleFormSubmit({ departure, destination }) {
    setRoute({ departure, destination });

    // mock NOTAM data for demonstration purposes
    setNotams([
      {
        id: "1",
        airport: departure,
        number: "A1234/26",
        message: "Runway 04/22 closed daily from 0200Z to 1000Z for maintenance.",
        startTime: "2026-04-17T02:00:00Z",
        endTime: "2026-04-20T10:00:00Z",
        category: "Runway",
      },
      {
        id: "2",
        airport: destination,
        number: "B5678/26",
        message: "Taxiway B lighting unavailable from 0000Z to 2359Z until further notice.",
        startTime: "2026-04-17T00:00:00Z",
        endTime: "2026-04-25T23:59:00Z",
        category: "Lighting",
      },
    ]);
  }

  return (
    <div style={{ padding: 24 }}>
      <h1>NOTAM Analyzer</h1>

      <AirportForm onSubmit={handleFormSubmit} />

      {route && (
        <p style={{ marginTop: "1.5rem" }}>
          Route: {route.departure} → {route.destination}
        </p>
      )}

      <NotamList notams={notams} />
    </div>
  );
}

export default App;