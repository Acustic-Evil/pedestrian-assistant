import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Sidebar from "./components/sidebar/Sidebar";
import MainContent from "./components/main-content/MainContent";
import History from "./components/history/History";
import IncidentDetails from "./components/incident/Incident";
import LoginPage from "./components/login/LoginPage";
import "./App.css";

const App = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    setIsAuthenticated(localStorage.getItem("isAuthenticated") === "true");
  }, []);

  const handleLogin = () => {
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem("isAuthenticated");
    window.location.href = "/login";
  };

  return (
    <Router>
      {isAuthenticated ? (
        <div className="app-container">
          <div className="logo-container">
            <img src="/tsodd.svg" alt="Logo" className="logo" />
          </div>
          <div className="main-content">
            <Sidebar onLogout={handleLogout} />
            <Routes>
              <Route path="/" element={<MainContent />} />
              <Route path="/history" element={<History />} />
              <Route path="/incidents/:id" element={<IncidentDetails />} />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </div>
        </div>
      ) : (
        <Routes>
          <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      )}
    </Router>
  );
};

export default App;
