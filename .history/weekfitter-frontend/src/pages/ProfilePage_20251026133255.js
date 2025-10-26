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
            value={formData.birthDate}import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import "../styles/ProfilePage.css";
import defaultAvatar from "../assets/neutral_avatar.png";

const ProfilePage = () => {
  const [userData, setUserData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    birthDate: "",
    gender: "",
    profilePicture: defaultAvatar,
  });

  const [loading, setLoading] = useState(true);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    // Načtení dat uživatele – zde simulace, můžeš nahradit fetch API
    const storedUser = localStorage.getItem("userEmail");
    if (storedUser) {
      // simulace – nahraď reálným načtením z backendu
      setUserData({
        firstName: localStorage.getItem("userName") || "Uživatel",
        lastName: " ",
        email: storedUser,
        birthDate: "1990-01-01",
        gender: "OTHER",
        profilePicture: defaultAvatar,
      });
    }
    setLoading(false);
  }, []);

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === "profilePicture" && files.length > 0) {
      const reader = new FileReader();
      reader.onload = () => {
        setUserData({ ...userData, profilePicture: reader.result });
      };
      reader.readAsDataURL(files[0]);
    } else {
      setUserData({ ...userData, [name]: value });
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSuccessMessage("");
    setErrorMessage("");

    try {
      // Zde bude volání backendu (PUT /api/users/update)
      console.log("Ukládám data:", userData);
      setSuccessMessage("Změny byly úspěšně uloženy!");
    } catch (error) {
      setErrorMessage("Chyba při ukládání změn.");
    }
  };

  if (loading) {
    return (
      <div className="profile-container">
        <div className="profile-card loading">Načítání profilu...</div>
      </div>
    );
  }

  return (
    <>
      <Header />
      <div className="profile-container">
        <div className="profile-card">
          <h2 className="profile-title">Můj profil</h2>

          {errorMessage && <div className="error-message">{errorMessage}</div>}
          {successMessage && (
            <div className="success-message">{successMessage}</div>
          )}

          <div className="profile-content">
            <div className="profile-photo-section">
              <img
                src={userData.profilePicture || defaultAvatar}
                alt="Profilová fotka"
                className="profile-photo"
              />
              <label htmlFor="profilePicture" className="upload-btn">
                Nahrát novou fotku
              </label>
              <input
                id="profilePicture"
                type="file"
                name="profilePicture"
                accept="image/*"
                onChange={handleChange}
                style={{ display: "none" }}
              />
            </div>

            <form className="profile-form" onSubmit={handleSave}>
              <div className="form-row">
                <label>Jméno</label>
                <input
                  type="text"
                  name="firstName"
                  value={userData.firstName}
                  onChange={handleChange}
                />
              </div>

              <div className="form-row">
                <label>Příjmení</label>
                <input
                  type="text"
                  name="lastName"
                  value={userData.lastName}
                  onChange={handleChange}
                />
              </div>

              <div className="form-row">
                <label>E-mail</label>
                <input
                  type="email"
                  name="email"
                  value={userData.email}
                  onChange={handleChange}
                  disabled
                />
              </div>

              <div className="form-row">
                <label>Datum narození</label>
                <input
                  type="date"
                  name="birthDate"
                  value={userData.birthDate}
                  onChange={handleChange}
                />
              </div>

              <div className="form-row">
                <label>Pohlaví</label>
                <select
                  name="gender"
                  value={userData.gender}
                  onChange={handleChange}
                >
                  <option value="MALE">Muž</option>
                  <option value="FEMALE">Žena</option>
                  <option value="OTHER">Jiné</option>
                </select>
              </div>

              <button type="submit" className="save-btn">
                Uložit změny
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default ProfilePage;

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
