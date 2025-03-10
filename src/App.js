import React from "react";
import { BrowserRouter as Router } from "react-router-dom";
import Sidebar from "./components/sidebar/Sidebar";
import MainContent from "./components/main-content/MainContent"; // Import MainContent
import "./App.css";

const App = () => {
  return (
    <Router>
      <div className="app-container">
        <div className="logo-container">
          <img src="/logo.svg" alt="Logo" className="logo" />
        </div>
        <div className="main-content">
          <Sidebar />
          <MainContent />
        </div>
      </div>
    </Router>
  );
};

export default App;
