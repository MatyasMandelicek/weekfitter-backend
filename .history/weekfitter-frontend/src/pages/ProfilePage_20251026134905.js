import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import "../styles/ProfilePage.css";
import defaultAvatar from "../assets/neutral_avatar.png";

const ProfilePage = () => {
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });

  const email = localStorage.getItem("userEmail");

  // Načtení dat uživatele z backendu
  useEffect(() => {
    const fetchUser = async () => {
      try {
            const response = await fetch(`http://localhost:8080/api/users/profile?email=${email}`);
        );
        if (response.ok) {
          const user = await response.json();
          setUserData(user);
        } else {
          setMessage({
            type: "error",
            text: "Nepodařilo se načíst údaje o uživateli.",
          });
        }
      } catch (error) {
        console.error("Chyba při načítání profilu:", error);
        setMessage({
          type: "error",
          text: "Chyba připojení k serveru.",
        });
      } finally {
        setLoading(false);
      }
    };
    if (email) fetchUser();
    else {
      setMessage({
        type: "error",
        text: "Nepodařilo se načíst e-mail z localStorage.",
      });
      setLoading(false);
    }
  }, [email]);

  // Změna údajů (formulář)
  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === "profilePicture" && files.length > 0) {
      const reader = new FileReader();
      reader.onload = () => {
        setUserData((prev) => ({ ...prev, profilePicture: reader.result }));
      };
      reader.readAsDataURL(files[0]);
    } else {
      setUserData((prev) => ({ ...prev, [name]: value }));
    }
  };

  // Uložení změn do databáze
  const handleSave = async (e) => {
    e.preventDefault();
    if (!userData) return;

    setSaving(true);
    setMessage({ type: "", text: "" });

    try {
      const response = await fetch("http://localhost:8080/api/users/update", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userData),
      });

      if (response.ok) {
        setMessage({ type: "success", text: "Změny byly úspěšně uloženy!" });
        localStorage.setItem("userName", userData.firstName);
      } else {
        setMessage({ type: "error", text: "Nepodařilo se uložit změny." });
      }
    } catch (error) {
      console.error("Chyba při ukládání profilu:", error);
      setMessage({ type: "error", text: "Chyba připojení k serveru." });
    } finally {
      setSaving(false);
    }
  };

  // Bezpečné zobrazení při načítání nebo chybějících datech
  if (loading || !userData) {
    return (
      <>
        <Header />
        <div className="profile-container">
          <div className="profile-card loading">Načítání profilu...</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />
      <div className="profile-container">
        <div className="profile-card">
          <h2 className="profile-title">Můj profil</h2>

          {message.text && (
            <div
              className={
                message.type === "error"
                  ? "error-message"
                  : "success-message"
              }
            >
              {message.text}
            </div>
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
                  value={userData.firstName || ""}
                  onChange={handleChange}
                />
              </div>

              <div className="form-row">
                <label>Příjmení</label>
                <input
                  type="text"
                  name="lastName"
                  value={userData.lastName || ""}
                  onChange={handleChange}
                />
              </div>

              <div className="form-row">
                <label>E-mail</label>
                <input type="email" value={userData.email} disabled />
              </div>

              <div className="form-row">
                <label>Datum narození</label>
                <input
                  type="date"
                  name="birthDate"
                  value={userData.birthDate || ""}
                  onChange={handleChange}
                />
              </div>

              <div className="form-row">
                <label>Pohlaví</label>
                <select
                  name="gender"
                  value={userData.gender || ""}
                  onChange={handleChange}
                >
                  <option value="MALE">Muž</option>
                  <option value="FEMALE">Žena</option>
                  <option value="OTHER">Jiné</option>
                </select>
              </div>

              <button type="submit" className="save-btn" disabled={saving}>
                {saving ? "Ukládám..." : "Uložit změny"}
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default ProfilePage;
