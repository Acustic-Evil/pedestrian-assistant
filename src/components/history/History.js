import React from "react";
import DashboardSection from "../dashboard-section/DashboardSection";
import "../../App.css"; // Reuse styling

const incidents = [
  { title: "User1", location: "ул. Пушкина", description: "Описание инцидента 1" },
  { title: "User2", location: "ул. Ленина", description: "Описание инцидента 2" },
  { title: "User3", location: "ул. Советская", description: "Описание инцидента 3" },
  { title: "User4", location: "ул. Гагарина", description: "Описание инцидента 4" },
  { title: "User5", location: "ул. Кирова", description: "Описание инцидента 5" },
];

const History = () => {
  return (
    <div className="content">
      <h1 style={{ marginBottom: "0.5rem" }}>История обращений</h1>

      <DashboardSection title="Все обращения">
        <div className="incidents-container full-history">
          {incidents.map((incident, index) => (
            <div key={index} className="incident-card">
              <strong>{incident.title}</strong>
              <p>{incident.location}</p>
              <p>{incident.description}</p>
              <a href="#">Читать далее...</a>
            </div>
          ))}
        </div>
      </DashboardSection>
    </div>
  );
};

export default History;
