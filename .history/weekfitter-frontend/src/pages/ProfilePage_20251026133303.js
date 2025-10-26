import React, { useState, useEffect } from "react";
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
