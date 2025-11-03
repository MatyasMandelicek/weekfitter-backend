import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import Logo from "../assets/Logo02.png";
import { useNavigate, useLocation } from "react-router-dom";
import "../styles/LoginPage.css";
import { API_URL } from "../lib/config";

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState({ email: "", password: "" });
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);

  // Pokud je uživatel už přihlášený, přesměruj ho rovnou na /home
  useEffect(() => {
    const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";
    if (isLoggedIn) {
      navigate("/home");
    }
  }, [navigate]);

  // Zobrazí hlášku z ResetPasswordPage
  useEffect(() => {
    if (location.state?.successMessage) {
      setSuccessMessage(location.state.successMessage);
      navigate(location.pathname, { replace: true });
    }
  }, [location, navigate]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      const res = await fetch(`${API_URL}/api/users/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (!res.ok) {
        setErrorMessage("Nesprávný e-mail nebo heslo.");
        setLoading(false);
        return;
      }

      const data = await res.json();

      if (data) {
        // Uložení přihlášení do localStorage
        localStorage.setItem("isLoggedIn", "true");
        localStorage.setItem("userEmail", formData.email);

        // Přesměrování na domovskou stránku
        navigate("/home");
      } else {
        setErrorMessage("Nesprávný e-mail nebo heslo.");
      }
    } catch (error) {
      console.error("Chyba při přihlašování:", error);
      setErrorMessage("Server momentálně nedostupný.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header />
      <main className="login-container">
        <div className="login-card">
          <img src={Logo} alt="Logo" className="login-logo" />
          <h2>Přihlášení</h2>

          {/* Hláška o úspěšném resetu hesla */}
          {successMessage && (
            <div className="success-message">{successMessage}</div>
          )}

          <form onSubmit={handleSubmit}>
            <input
              type="email"
              name="email"
              placeholder="E-mail"
              value={formData.email}
              onChange={handleChange}
              required
            />
            <input
              type="password"
              name="password"
              placeholder="Heslo"
              value={formData.password}
              onChange={handleChange}
              required
            />

            <div
              className="forgot-password-link"
              onClick={() => navigate("/forgot-password")}
            >
              Zapomenuté heslo?
            </div>

            {errorMessage && (
              <div className="error-message">{errorMessage}</div>
            )}

            <button type="submit" className="primary-btn" disabled={loading}>
              {loading ? "Přihlašuji..." : "Přihlásit se"}
            </button>
          </form>

          <p>
            Nemáte účet?{" "}
            <span
              className="register-link"
              onClick={() => navigate("/register")}
            >
              Zaregistrujte se
            </span>
          </p>
        </div>
      </main>
    </>
  );
};

export default LoginPage;
