import React, { useState } from "react";
import Card from "../card/Card";
import "./LoginPage.css";
import {
    useNavigate,
    Link
  } from "react-router-dom";

const API_URL = process.env.REACT_APP_API_URL;

const LoginPage = ({ onLogin }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch(`${API_URL}/auth/`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) throw new Error("Неверный логин или пароль");

      const data = await response.json();
      localStorage.setItem("token", data.token); // Store JWT
      
      // Вызываем функцию onLogin для обновления состояния аутентификации в App.js
      if (onLogin) {
        onLogin();
      }
      
      navigate("/");

    } catch (error) {
      setErrorMsg(error.message);
    }
  };

  return (
    <div className="login-page">
      <div className="logo-container">
        <img src="/tsodd.svg" alt="Logo" className="logo" />
      </div>

      <div className="login-card-wrapper">
        <Card>
          <h2 style={{ textAlign: "center", marginBottom: "1rem" }}>
            Вход в систему
          </h2>
          {errorMsg && (
            <p style={{ color: "red", fontSize: "0.9rem", marginBottom: "1rem" }}>
              {errorMsg}
            </p>
          )}
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
            <div className="forgot-password-container">
              <Link to="/forgot-password" className="forgot-password-link">Забыли пароль?</Link>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
};

export default LoginPage;
