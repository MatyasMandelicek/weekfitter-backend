import React, { useState } from "react";
import Header from "../components/Header";
import Logo from "../assets/Logo02.png";
import { useNavigate } from "react-router-dom";
import "../styles/LoginPage.css";

const LoginPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [errorMessage, setErrorMessage] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      // Pošle přihlašovací údaje na backend
      const res = await fetch("http://localhost:8080/api/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      // Backend vrací boolean true/false
      const success = await res.json();

      if (success) {
        // Uloží info o přihlášení do localStorage
        localStorage.setItem("isLoggedIn", "true");
        localStorage.setItem("userEmail", formData.email);

        console.log("Uživatel přihlášen:", formData.email);
        navigate("/");
      } else {
        setErrorMessage("Nesprávný e-mail nebo heslo.");
      }
    } catch (error) {
      console.error("Chyba při přihlašování:", error);
      setErrorMessage("Server momentálně nedostupný.");
    }
  };

  return (
    <>
      <Header />
      <main className="login-container">
        <div className="login-card">
          <img src={Logo} alt="Logo" className="login-logo" />
          <h2>Přihlášení</h2>
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
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            <button type="submit">Přihlásit se</button>
          </form>
          <p>
            Nemáte účet?{" "}
            <span
              className="register-link"
              onClick={() => navigate("/register")}
              style={{ cursor: "pointer" }}
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
