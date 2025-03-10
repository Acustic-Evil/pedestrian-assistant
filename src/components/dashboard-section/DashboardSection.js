import React from "react";
import "./DashboardSection.css";

const DashboardSection = ({ title, children }) => {
  return (
    <div className="dashboard-section">
      <h2 className="section-title">{title}</h2>
      <div className="section-content">{children}</div>
    </div>
  );
};

export default DashboardSection;
