import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "../components/Header";
import "../styles/LoginPage.css";

const ResetPasswordPage = () => {
  const { token } = useParams();
  const navigate = useNavigate();
  const [newPassword, setNewPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");

    try {
      const res = await fetch("http://localhost:8080/api/users/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, newPassword }),
      });

      const text = await res.text();
      if (res.ok) {
        setMessage("✅ Heslo bylo úspěšně změněno!");
        setTimeout(() => navigate("/login"), 2000);
      } else {
        setMessage("❌ Neplatný nebo expirovaný odkaz.");
      }
    } catch (error) {
      setMessage("⚠️ Server je momentálně nedostupný.");
    }
  };

  return (
    <>
      <Header />
      <main className="login-container">
        <div className="login-card">
          <h2>Nastavit nové heslo</h2>
          <form onSubmit={handleSubmit}>
            <input
              type="password"
              placeholder="Nové heslo"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <button type="submit">Změnit heslo</button>
          </form>
          {message && <div className="success-message">{message}</div>}
        </div>
      </main>
    </>
  );
};

export default ResetPasswordPage;
