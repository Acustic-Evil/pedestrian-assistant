import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Sidebar from "./components/sidebar/Sidebar";
import MainContent from "./components/main-content/MainContent";
import History from "./components/history/History";
import IncidentDetails from "./components/incident/Incident";
import "./App.css";

const App = () => {
  return (
    <Router>
      <div className="app-container">
        <div className="logo-container">
          <img src="/tsodd.svg" alt="Logo" className="logo" />
        </div>
        <div className="main-content">
          <Sidebar />
          <Routes>
            <Route path="/" element={<MainContent />} />
            <Route path="/history" element={<History />} />
            <Route path="/incidents/:id" element={<IncidentDetails />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
};

export default App;
