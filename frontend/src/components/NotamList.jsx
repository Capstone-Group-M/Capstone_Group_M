import React from "react";

function NotamList({ notams }) {
  if (!notams || notams.length === 0) {
    return <p>No NOTAMs found.</p>;
  }

  return (
    <div>
      <h2>NOTAM Results</h2>
      <ul style={{ listStyle: "none", padding: 0 }}>
        {notams.map((notam, index) => (
          <li
            key={index}
            style={{
              border: "1px solid #ccc",
              borderRadius: "8px",
              padding: "12px",
              marginBottom: "10px",
              backgroundColor: "#f9f9f9",
            }}
          >
            {typeof notam === "string" ? (
              <p>{notam}</p>
            ) : (
              <>
                <strong>{notam.title || "NOTAM"}</strong>
                <p>{notam.message}</p>
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default NotamList;