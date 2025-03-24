import React, { useState } from "react";
import Card from "../card/Card";
import "./LoginPage.css";

const LoginPage = ({ onLogin }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = (e) => {
    e.preventDefault();

    if (username.trim() && password.trim()) {
      localStorage.setItem("isAuthenticated", "true");
      onLogin(); // trigger auth state
    }
  };

  return (
    <div className="login-page">
      <div className="logo-container">
        <img src="/tsodd.svg" alt="Logo" className="logo" />
      </div>

      <div className="login-card-wrapper">
        <Card>
          <h2 style={{ textAlign: "center", marginBottom: "1rem" }}>Вход в систему</h2>
          <form onSubmit={handleLogin} className="login-form">
            <input
              type="text"
              placeholder="Имя пользователя"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Пароль"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <button type="submit">Войти</button>
          </form>
        </Card>
      </div>
    </div>
  );
};

export default LoginPage;
