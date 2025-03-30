import React, { useState, useEffect } from "react";
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import Card from "../card/Card";

const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [status, setStatus] = useState("");
  const [statusType, setStatusType] = useState("error"); // "error" или "success"
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isTokenValid, setIsTokenValid] = useState(true);

  useEffect(() => {
    const validate = async () => {
      try {
        const res = await fetch(
          `${process.env.REACT_APP_API_URL}/auth/reset/validate?token=${token}`
        );
        if (!res.ok) {
          setIsTokenValid(false);
          throw new Error("Invalid or expired token");
        }
        setIsTokenValid(true);
      } catch (err) {
        setIsTokenValid(false);
        setStatusType("error");
        setStatus("Ошибка: ссылка недействительна или устарела.");
      }
    };

    if (token) validate();
    else {
      setIsTokenValid(false);
      setStatusType("error");
      setStatus("Ошибка: отсутствует токен для сброса пароля.");
    }
  }, [token]);

  const handleReset = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setStatusType("error");
      return setStatus("Пароли не совпадают");
    }

    setIsSubmitting(true);
    try {
      const res = await fetch(`${process.env.REACT_APP_API_URL}/auth/reset/confirm`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, newPassword }),
      });

      if (res.ok) {
        setStatusType("success");
        setStatus("Пароль успешно изменен!");
        // Перенаправление на страницу входа через 3 секунды после успешного сброса пароля
        setTimeout(() => {
          navigate("/login");
        }, 3000);
      } else {
        const data = await res.text();
        setStatusType("error");
        setStatus(data || "Ошибка при сбросе пароля");
      }
    } catch (err) {
      setStatusType("error");
      setStatus("Ошибка при сбросе пароля. Пожалуйста, попробуйте позже.");
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
          <h2 style={{ textAlign: "center", marginBottom: "1rem" }}>Новый пароль</h2>
          {!isTokenValid ? (
            <div>
              <p style={{ 
                color: "red", 
                fontSize: "0.9rem", 
                marginBottom: "1rem",
                textAlign: "center" 
              }}>
                {status}
              </p>
              <div style={{ textAlign: "center", marginTop: "1rem" }}>
                <Link to="/forgot-password" style={{ color: "#0056b3", textDecoration: "none", marginRight: "1rem" }}>
                  Запросить новую ссылку
                </Link>
                <Link to="/login" style={{ color: "#0056b3", textDecoration: "none" }}>
                  Вернуться на страницу входа
                </Link>
              </div>
            </div>
          ) : (
            <>
              <form onSubmit={handleReset} className="login-form">
                <input
                  type="password"
                  placeholder="Новый пароль"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  disabled={isSubmitting}
                />
                <input
                  type="password"
                  placeholder="Подтвердите пароль"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  disabled={isSubmitting}
                />
                <button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? "Обработка..." : "Сбросить пароль"}
                </button>
              </form>
              {status && (
                <p style={{ 
                  color: statusType === "success" ? "green" : "red", 
                  fontSize: "0.9rem", 
                  marginBottom: "1rem",
                  textAlign: "center" 
                }}>
                  {status}
                </p>
              )}
              {!isSubmitting && statusType !== "success" && (
                <div style={{ textAlign: "center", marginTop: "1rem" }}>
                  <Link to="/login" style={{ color: "#0056b3", textDecoration: "none" }}>
                    Вернуться на страницу входа
                  </Link>
                </div>
              )}
            </>
          )}
        </Card>
      </div>
    </div>
  );
};

export default ResetPasswordPage;
