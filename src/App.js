import React, { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Sidebar from "./components/sidebar/Sidebar";
import MainContent from "./components/main-content/MainContent";
import History from "./components/history/History";
import IncidentDetails from "./components/incident/Incident";
import LoginPage from "./components/login/LoginPage";
import ResetPasswordPage from "./components/login/ResetPasswordPage";
import ForgotPasswordPage from "./components/login/ForgotPasswordPage"
import "./App.css";

const App = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(
    !!localStorage.getItem("token")
  );

  const handleLogin = () => setIsAuthenticated(true);
  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
  };

  // Render login, forgot password and reset password pages without sidebar
  if (!isAuthenticated) {
    return (
      <Router>
        <Routes>
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="*" element={<LoginPage onLogin={handleLogin} />} />
        </Routes>
      </Router>
    );
  }

  return (
    <Router>
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
            {/* These routes are now handled in the unauthenticated section */}
            <Route path="/login" element={<LoginPage />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
};

export default App;
