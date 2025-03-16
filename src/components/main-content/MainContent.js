import React, { useState, useEffect } from "react";
import { Routes, Route } from "react-router-dom";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { format } from "date-fns"; // Helps format the selected date
import Card from "../card/Card";
import Graph from "../graph/Graph";
import DashboardSection from "../dashboard-section/DashboardSection";
import "../../App.css"; // Keep styling consistent

const incidents = [
  {
    title: "User",
    location: "Откуда: улица Пушкина",
    description: "Они тут совсем с ума сошли!!! Их трое на самокате!",
  },
  {
    title: "User",
    location: "Откуда: улица Пушкина",
    description: "Они тут совсем с ума сошли!!! Их трое на самокате!",
  },
  {
    title: "User",
    location: "Откуда: улица Пушкина",
    description: "Они тут совсем с ума сошли!!! Их трое на самокате!",
  },
];

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 12) return "Доброе утро";
  if (hour >= 12 && hour < 18) return "Добрый день";
  return "Добрый вечер";
};

const MainContent = () => {
  const [greeting, setGreeting] = useState(getGreeting());
  const [selectedDate, setSelectedDate] = useState(new Date()); // Manage selected date

  useEffect(() => {
    const interval = setInterval(() => {
      setGreeting(getGreeting());
    }, 60000);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="content">
      <h1 style={{ marginBottom: "0.5rem" }}>{greeting}</h1>

      <Routes>
        {/* Default Dashboard Content */}
        <Route
          path="/"
          element={
            <>
              {/* Move "Количество обращений за" Here */}
              <DashboardSection
                title={
                  <span>
                    Количество обращений за{" "}
                    <span style={{ textDecoration: "underline", cursor: "pointer" }}>
                      <DatePicker
                        selected={selectedDate}
                        onChange={(date) => setSelectedDate(date)}
                        dateFormat="dd-MM-yyyy"
                        customInput={
                          <span style={{ textDecoration: "underline", cursor: "pointer", color: "blue" }}>
                            {format(selectedDate, "dd-MM-yyyy")}
                          </span>
                        }
                      />
                    </span>
                  </span>
                }
              >
                <Card>
                  <Graph selectedDate={selectedDate} />
                </Card>
                <Card>{/* Placeholder for another card */}</Card>
              </DashboardSection>

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
            </>
          }
        />

        {/* History Page */}
        <Route
          path="/history"
          element={
            <DashboardSection title="История обращений">
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
          }
        />
      </Routes>
    </div>
  );
};

export default MainContent;
