import { useState } from "react";

function AirportForm({ onSubmit, isSubmitting }) {
  const [departure, setDeparture] = useState("");
  const [destination, setDestination] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit({ departure, destination });
  }

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Departure Airport"
        value={departure}
        onChange={(e) => setDeparture(e.target.value)}
        required
      />
      <input
        type="text"
        placeholder="Destination Airport"
        value={destination}
        onChange={(e) => setDestination(e.target.value)}
        required
      />
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Loading..." : "Search"}
      </button>
    </form>
  );
}

export default AirportForm;