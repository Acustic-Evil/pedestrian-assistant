import React, { useState } from "react";
import Card from "../card/Card";
import { Link } from "react-router-dom";

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("error"); // "error" или "success"
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleResetRequest = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      const response = await fetch(
        `${process.env.REACT_APP_API_URL}/auth/reset/request?email=${encodeURIComponent(email)}`,
        { method: "POST" }
      );
      
      if (response.ok) {
        setMessageType("success");
        setMessage("Ссылка для сброса пароля отправлена на вашу почту.");
      } else {
        const data = await response.json();
        setMessageType("error");
        
        // Проверяем, содержит ли ошибка информацию о дубликате
        if (data.error && data.error.includes("duplicate key value")) {
          setMessage("Ссылка для сброса пароля уже была отправлена на этот адрес. Пожалуйста, проверьте вашу почту или попробуйте позже.");
        } else {
          setMessage(data.error || "Ошибка при отправке запроса.");
        }
      }
    } catch (error) {
      setMessageType("error");
      setMessage("Ошибка при отправке запроса. Пожалуйста, попробуйте позже.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="logo-container">
        <img src="/tsodd.svg" alt="Logo" className="logo" />
      </div>
      <div className="login-card-wrapper">
        <Card>
          <h2 style={{ textAlign: "center", marginBottom: "1rem" }}>Сброс пароля</h2>
          <form onSubmit={handleResetRequest} className="login-form">
            <input
              type="email"
              placeholder="Введите ваш email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={isSubmitting}
            />
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Отправка..." : "Отправить ссылку для сброса"}
            </button>
          </form>
          {message && (
            <p 
              style={{ 
                color: messageType === "success" ? "green" : "red", 
                fontSize: "0.9rem", 
                marginBottom: "1rem",
                textAlign: "center" 
              }}
            >
              {message}
            </p>
          )}
          <div style={{ textAlign: "center", marginTop: "1rem" }}>
            <Link to="/login" style={{ color: "#0056b3", textDecoration: "none" }}>
              Вернуться на страницу входа
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
