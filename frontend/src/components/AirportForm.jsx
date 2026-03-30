import { useState } from "react";
import "./AirportForm.css";

function AirportForm({ onSubmit }) {
  const [departure, setDeparture] = useState("");
  const [destination, setDestination] = useState("");

  function handleSubmit(e) {
    e.preventDefault();
    if (!departure.trim() || !destination.trim()) return;
    onSubmit({
      departure: departure.trim().toUpperCase(),
      destination: destination.trim().toUpperCase(),
    });
  }

  return (
    <form className="airport-form" onSubmit={handleSubmit}>
      <div className="airport-form-field">
        <label htmlFor="departure">Departure Airport (ICAO)</label>
        <input
          id="departure"
          type="text"
          placeholder="e.g. KJFK"
          value={departure}
          onChange={(e) => setDeparture(e.target.value.toUpperCase())}
          maxLength={4}
        />
      </div>
      <div className="airport-form-field">
        <label htmlFor="destination">Destination Airport (ICAO)</label>
        <input
          id="destination"
          type="text"
          placeholder="e.g. KLAX"
          value={destination}
          onChange={(e) => setDestination(e.target.value.toUpperCase())}
          maxLength={4}
        />
      </div>
      <button type="submit">Search NOTAMs</button>
    </form>
  );
}

export default AirportForm;
