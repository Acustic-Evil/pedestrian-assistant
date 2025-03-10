import "./Sidebar.css";
import React from "react";

const navItems = [
  { name: "История обращений", path: "/history" },
  { name: "Тепловая карта", path: "/heatmap" },
];

const Sidebar = () => {
  return (
    <div className="sidebar-container">
      <div className="sidebar">
        <div className="sidebar-main">
          <div className="sidebar-title">Сводка</div>
          <div className="nav-links">
            {navItems.map((item, index) => (
              <a key={index} href={item.link} className="nav-item">
                {item.name}
              </a>
            ))}
          </div>
          <a className="nav-item help">Помощь</a>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
