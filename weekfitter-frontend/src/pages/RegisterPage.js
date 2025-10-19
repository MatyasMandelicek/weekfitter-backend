import React, { useState } from "react";
import Header from "../components/Header";
import Logo from "../assets/Logo02.png";
import { useNavigate } from "react-router-dom";
import "../styles/RegisterPage.css";

const RegisterPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    birthDate: "",
    profilePicture: "",
  });

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === "profilePicture" && files.length > 0) {
      const reader = new FileReader();
      reader.onload = () => {
        setFormData({ ...formData, profilePicture: reader.result });
      };
      reader.readAsDataURL(files[0]);
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log("Odesílám registraci:", formData);

    try {
      const res = await fetch("http://localhost:8080/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (res.ok) {
        alert("Registrace proběhla úspěšně!");
        navigate("/login");
      } else {
        alert("Chyba při registraci, zkuste to znovu.");
      }
    } catch (error) {
      console.error("Chyba při registraci:", error);
      alert("Server je momentálně nedostupný.");
    }
  };

  return (
    <>
      <Header />
      <main className="register-container">
        <div className="register-card">
          <img src={Logo} alt="Logo" className="register-logo" />
          <h2>Registrace</h2>
          <form onSubmit={handleSubmit}>
            <input
              type="text"
              name="firstName"
              placeholder="Jméno"
              value={formData.firstName}
              onChange={handleChange}
              required
            />
            <input
              type="text"
              name="lastName"
              placeholder="Příjmení"
              value={formData.lastName}
              onChange={handleChange}
              required
            />
            <input
              type="date"
              name="birthDate"
              value={formData.birthDate}
              onChange={handleChange}
              required
            />
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
            <input
              type="file"
              name="profilePicture"
              accept="image/*"
              onChange={handleChange}
            />
            <button type="submit">Registrovat</button>
          </form>
          <p>
            Máte již účet?{" "}
            <span
              className="login-link"
              onClick={() => navigate("/login")}
              style={{ cursor: "pointer" }}
            >
              Přihlásit se
            </span>
          </p>
        </div>
      </main>
    </>
  );
};

export default RegisterPage;
