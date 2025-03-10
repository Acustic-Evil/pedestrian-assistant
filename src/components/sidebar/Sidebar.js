import "./Sidebar.css";
import React from "react";
import { NavLink } from "react-router-dom";

const navItems = [
  { name: "Сводка", path: "/" }, // Main page (summary)
  { name: "История обращений", path: "/history" },
];

const Sidebar = () => {
  return (
    <div className="sidebar-container">
      <div className="sidebar">
        <div className="sidebar-main">
          <div className="nav-links">
            {navItems.map((item, index) => (
              <NavLink
                key={index}
                to={item.path}
                className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
              >
                {item.name}
              </NavLink>
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