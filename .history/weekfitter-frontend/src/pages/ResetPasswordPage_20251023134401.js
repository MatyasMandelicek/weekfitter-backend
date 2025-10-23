import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "../components/Header";
import "../styles/LoginPage.css";

const ResetPasswordPage = () => {
  const { token } = useParams();
  const navigate = useNavigate();
  const [newPassword, setNewPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const res = await fetch("http://localhost:8080/api/users/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, newPassword }),
      });

      if (res.ok) {
        // po úspěchu přesměrujeme uživatele na login s hláškou
        navigate("/login", {
          state: { successMessage: "✅ Heslo bylo úspěšně změněno. Přihlaste se novým heslem." },
        });
      } else {
        setMessage("❌ Neplatný nebo expirovaný odkaz.");
      }
    } catch (error) {
      console.error("Chyba při resetování hesla:", error);
      setMessage("⚠️ Server je momentálně nedostupný.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header />
      <main className="login-container">
        <div className="login-card">
          <h2>Obnovení hesla</h2>
          <form onSubmit={handleSubmit}>
            <input
              type="password"
              placeholder="Nové heslo"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <button type="submit" disabled={loading}>
              {loading ? "Ukládám..." : "Změnit heslo"}
            </button>
          </form>

          {message && <div className="error-message">{message}</div>}
        </div>
      </main>
    </>
  );
};

export default ResetPasswordPage;
