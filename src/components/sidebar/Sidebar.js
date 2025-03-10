import "./Sidebar.css";
import React, { useState } from "react";

const navItems = [
  { name: "История обращений", path: "/history" },
  { name: "Тепловая карта", path: "/heatmap" },
];

const Sidebar = () => {
  const [activePage, setActivePage] = useState("/history");

  return (
    <div className="sidebar-container">
      <div className="sidebar">
        <div className="sidebar-main">
          <div className="sidebar-title">Сводка</div>
          <div className="nav-links">
            {navItems.map((item, index) => (
              <a
                key={index}
                href={item.path}
                className={`nav-item ${activePage === item.path ? "active" : ""}`}
                onClick={() => setActivePage(item.path)}
              >
                {item.name}
              </a>
            ))}
          </div>
        </div>
        <div className="help-container">
          <a className="nav-item help">Помощь</a>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
