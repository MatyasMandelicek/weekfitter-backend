import React, { useState } from "react";
import Header from "../components/Header";
import Logo from "../assets/Logo02.png";
import { useNavigate } from "react-router-dom";
import "../styles/LoginPage.css";

const LoginPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: "", password: "" });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Přihlášený uživatel:", formData);
    
    localStorage.setItem("isLoggedIn", "true");
    localStorage.setItem("userEmail", formData.email);

    navigate("/plan");
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
            <button type="submit">Přihlásit se</button>
          </form>
          <p>
            Nemáte účet? <span className="register-link">Zaregistrujte se</span>
          </p>
        </div>
      </main>
    </>
  );
};

export default LoginPage;
