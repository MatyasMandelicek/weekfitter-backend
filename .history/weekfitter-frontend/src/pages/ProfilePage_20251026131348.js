import React, { useState } from "react";
import Header from "../components/Header";
import Logo from "../assets/Logo02.png";
import MaleAvatar from "../assets/male_avatar.png";
import FemaleAvatar from "../assets/female_avatar.png";
import NeutralAvatar from "../assets/neutral_avatar.png";
import { useNavigate } from "react-router-dom";
import "../styles/RegisterPage.css";

const RegisterPage = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    gender: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    birthDate: "",
    profilePicture: "",
  });

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;

    // Pokud uživatel vybere pohlaví, nastav odpovídající avatar
    if (name === "gender") {
      let avatar = "";
      if (value === "male") avatar = MaleAvatar;
      else if (value === "female") avatar = FemaleAvatar;
      else avatar = NeutralAvatar;

      setFormData((prev) => ({
        ...prev,
        gender: value,
        profilePicture: avatar,
      }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");
    setLoading(true);

    try {
      // TODO: volání backend API pro registraci
      console.log("Odesílám data:", formData);
      setTimeout(() => {
        setSuccessMessage("Registrace proběhla úspěšně!");
        setLoading(false);
        setTimeout(() => navigate("/login"), 1500);
      }, 1200);
    } catch (error) {
      setErrorMessage("Chyba při registraci. Zkuste to znovu.");
      setLoading(false);
    }
  };

  return (
    <div className="register-container">
      <Header />
      <div className="register-card">
        <img src={Logo} alt="WeekFitter Logo" className="register-logo" />
        <h2>Registrace</h2>

        {errorMessage && <div className="error-message">{errorMessage}</div>}
        {successMessage && (
          <div className="success-message">{successMessage}</div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="gender-selection">
            <h4>Zvolte pohlaví</h4>
            <div className="gender-options">
              <label>
                <input
                  type="radio"
                  name="gender"
                  value="male"
                  checked={formData.gender === "male"}
                  onChange={handleChange}
                />
                <img src={MaleAvatar} alt="Muž" />
              </label>

              <label>
                <input
                  type="radio"
                  name="gender"
                  value="female"
                  checked={formData.gender === "female"}
                  onChange={handleChange}
                />
                <img src={FemaleAvatar} alt="Žena" />
              </label>

              <label>
                <input
                  type="radio"
                  name="gender"
                  value="neutral"
                  checked={formData.gender === "neutral"}
                  onChange={handleChange}
                />
                <img src={NeutralAvatar} alt="Ostatní" />
              </label>
            </div>
          </div>

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
            type="date"
            name="birthDate"
            value={formData.birthDate}
            onChange={handleChange}
            required
          />

          <button type="submit" disabled={loading}>
            {loading ? "Probíhá registrace..." : "Registrovat se"}
          </button>
        </form>

        <div
          className="login-link"
          onClick={() => navigate("/login")}
        >
          Máte už účet? Přihlaste se
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
