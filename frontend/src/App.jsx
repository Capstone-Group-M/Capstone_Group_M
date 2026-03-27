import { useState } from "react";
import AirportForm from "./components/AirportForm";
import "./App.css";

function App() {
  const [route, setRoute] = useState(null);

  function handleFormSubmit({ departure, destination }) {
    setRoute({ departure, destination });
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
    </div>
  );
}

export default App;
