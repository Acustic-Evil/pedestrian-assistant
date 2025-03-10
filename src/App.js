import React from "react";
import Sidebar from "./components/sidebar/Sidebar";
import "./App.css";

const App = () => {
  return (
    <div className="app-container">
      <div className="logo-container">
        <img src="/logo.png" alt="Logo" className="logo" />
      </div>
      <div className="main-content">
        <Sidebar />
        <div className="content">
          <h1>Welcome to the Dashboard</h1>
          <p>This is the main content area.</p>
        </div>
      </div>
    </div>
  );
};

export default App;
