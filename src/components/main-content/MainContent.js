import React, { useState, useEffect } from "react";
import { Routes, Route } from "react-router-dom";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import Card from "../card/Card";
import Graph from "../graph/Graph";
import DashboardSection from "../dashboard-section/DashboardSection";
import "../../App.css";


const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 12) return "Доброе утро";
  if (hour >= 12 && hour < 18) return "Добрый день";
  return "Добрый вечер";
};

const MainContent = () => {
  const [greeting, setGreeting] = useState(getGreeting());
  const [dateRange, setDateRange] = useState([new Date(), new Date()]); // [startDate, endDate]
  const [startDate, endDate] = dateRange;
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);

  const API_URL = process.env.REACT_APP_API_URL;


  useEffect(() => {
    const interval = setInterval(() => {
      setGreeting(getGreeting());
    }, 60000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    fetchRecentIncidents();
  }, []);

  const fetchRecentIncidents = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_URL}/incidents`);
      if (!response.ok) throw new Error("Failed to fetch data");
      const allIncidents = await response.json();

      // Sort by `createdAt` (most recent first) & take the last 5 incidents
      const recentIncidents = allIncidents
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5);

      setIncidents(recentIncidents);
    } catch (error) {
      console.error("Error fetching incidents:", error);
      setIncidents([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="content">
      <h1 style={{ marginBottom: "0.5rem" }}>{greeting}</h1>

      <Routes>
        <Route
          path="/"
          element={
            <>
              {/* Dashboard Section with Graph */}
              <DashboardSection title="Количество обращений">
                <div className="dashboard-cards">
                  {/* Graph Card */}
                  <Card className="card">
                    <Graph startDate={startDate} endDate={endDate} />
                  </Card>

                  {/* Calendar Card */}
                  <Card className="calendar-card">
                    <div className="calendar-container">
                      <DatePicker
                        selectsRange={true}
                        startDate={startDate}
                        endDate={endDate}
                        onChange={(update) => setDateRange(update)}
                        dateFormat="dd-MM-yyyy"
                        inline
                      />
                    </div>
                  </Card>
                </div>
              </DashboardSection>

              {/* Recent Incidents Section */}
              <DashboardSection title="Последние обращения">
                <div className="incidents-container main-screen-container">
                  {loading ? (
                    <p>Загрузка...</p>
                  ) : incidents.length === 0 ? (
                    <p style={{ color: "gray" }}>Нет недавних обращений</p>
                  ) : (
                    incidents.map((incident, index) => (
                      <div key={index} className="incident-card">
                        <strong>{incident.title}</strong>
                        <p>{incident.location}</p>
                        <p>{incident.description}</p>
                        <a href="#">Читать далее...</a>
                      </div>
                    ))
                  )}
                </div>
              </DashboardSection>
            </>
          }
        />
      </Routes>
    </div>
  );
};

export default MainContent;
