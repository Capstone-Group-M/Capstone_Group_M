import "./NotamList.css";

function formatDate(value) {
  if (!value) return "N/A";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return date.toLocaleString();
}

function NotamList({ notams = [], loading = false, error = "" }) {
  if (loading) {
    return <div className="notam-list-state">Loading NOTAMs...</div>;
  }

  if (error) {
    return <div className="notam-list-state notam-list-error">{error}</div>;
  }

  if (!notams.length) {
    return <div className="notam-list-state">No NOTAMs to display.</div>;
  }

  return (
    <section className="notam-list">
      <h2>NOTAM Results</h2>

      <div className="notam-list-items">
        {notams.map((notam, index) => (
          <article
            key={notam.id || `${notam.airport || "notam"}-${index}`}
            className="notam-card"
          >
            <div className="notam-card-header">
              <span className="notam-airport">{notam.airport || "Unknown Airport"}</span>
              <span className="notam-number">{notam.number || `NOTAM ${index + 1}`}</span>
            </div>

            <p className="notam-message">{notam.message || "No NOTAM text available."}</p>

            <div className="notam-meta">
              <div>
                <strong>Start:</strong> {formatDate(notam.startTime)}
              </div>
              <div>
                <strong>End:</strong> {formatDate(notam.endTime)}
              </div>
              <div>
                <strong>Category:</strong> {notam.category || "General"}
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

export default NotamList;