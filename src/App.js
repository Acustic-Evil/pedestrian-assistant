import React, { useState, useEffect } from "react";
import Sidebar from "./components/sidebar/Sidebar";
import Card from "./components/card/Card";
import Graph from "./components/graph/Graph";
import DashboardSection from "./components/dashboard-section/DashboardSection";
import "./App.css";

const incidents = [
  {
    title: "User",
    location: "Откуда: улица Пушкина",
    description: "Они тут совсем с ума сошли!!! У них водник прямо на самокате!",
  },
  {
    title: "User",
    location: "Откуда: улица Пушкина",
    description: "Они тут совсем с ума сошли!!! У них водник прямо на самокате!",
  },
  {
    title: "User",
    location: "Откуда: улица Пушкина",
    description: "Они тут совсем с ума сошли!!! У них водник прямо на самокате!",
  },
];

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 12) return "Доброе утро";
  if (hour >= 12 && hour < 18) return "Добрый день";
  return "Добрый вечер";
};


const App = () => {
  const [greeting, setGreeting] = useState(getGreeting());

  useEffect(() => {
    const interval = setInterval(() => {
      setGreeting(getGreeting()); // Update greeting dynamically
    }, 60000); // Check every minute

    return () => clearInterval(interval); // Cleanup interval
  }, []);

  return (
    <div className="app-container">
      <div className="logo-container">
        <img src="/logo.png" alt="Logo" className="logo" />
      </div>
      <div className="main-content">
        <Sidebar />
        <div className="content">
        <h1 style={{ marginBottom: "0.5rem" }}>{greeting}</h1>

          {/* Analytics Section (Row Layout) */}
          <DashboardSection
            title="Количество обращений за сутки"
            className="analytics-section"
          >
            <Card>
              <Graph />
            </Card>
            <Card>{/* Placeholder for another card */}</Card>
          </DashboardSection>

          {/* Recent Incidents Section (Column Layout) */}
          <DashboardSection title="Последние обращения">
            <div className="incidents-container">
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
      </div>
    </div>
  );
};

export default App;
