import React, { useEffect, useState, useCallback } from "react";
import Header from "../components/Header";
import "../styles/ProfilePage.css";

const ProfilePage = () => {
  const [user, setUser] = useState(null);
  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(false);
  const email = localStorage.getItem("userEmail");

  const loadProfile = useCallback(async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/users/profile?email=${email}`);
      if (res.ok) {
        const data = await res.json();
        setUser(data);
        setForm({
          firstName: data.firstName || "",
          lastName: data.lastName || "",
          birthDate: data.birthDate || "",
          gender: data.gender || "",
        });
      } else {
        console.error("Nepodařilo se načíst profil");
      }
    } catch (err) {
      console.error("Chyba při načítání profilu:", err);
    }
  }, [email]);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch(`http://localhost:8080/api/users/profile?email=${email}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      if (res.ok) {
        alert("Profil byl úspěšně uložen");
        loadProfile();
      } else {
        alert("Nepodařilo se uložit profil");
      }
    } finally {
      setLoading(false);
    }
  };

  const handlePhotoUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);
    setLoading(true);
    try {
      const res = await fetch(`http://localhost:8080/api/users/upload-photo?email=${email}`, {
        method: "POST",
        body: formData,
      });
      if (res.ok) {
        alert("Fotka byla úspěšně nahrána");
        loadProfile();
      } else {
        alert("Chyba při nahrávání fotky");
      }
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return (
      <>
        <Header />
        <div className="profile-container">
          <div className="profile-card loading">Načítám profil...</div>
        </div>
      </>
    );
  }

    const avatarSrc =
    user.photo && !user.photo.includes("/assets/")
        ? `http://localhost:8080${user.photo}`
        : user.gender === "FEMALE"
        ? require("../assets/default-avatar-female.png")
        : user.gender === "MALE"
        ? require("../assets/default-avatar.png")
        : require("../assets/default-avatar-other.png");


  return (
    <>
      <Header />
      <div className="profile-container">
        <div className="profile-card">
          <h2 className="profile-title">Můj profil</h2>

          <div className="profile-content">
            <div className="profile-photo-section">
              <img src={avatarSrc} alt="Profil" className="profile-photo" />
              <label className="upload-btn">
                {loading ? "Nahrávám..." : "Změnit fotku"}
                <input
                  type="file"
                  accept="image/*"
                  onChange={handlePhotoUpload}
                  disabled={loading}
                  hidden
                />
              </label>
            </div>

            <form className="profile-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label>Jméno</label>
                <input
                  name="firstName"
                  value={form.firstName}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-row">
                <label>Příjmení</label>
                <input
                  name="lastName"
                  value={form.lastName}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-row">
                <label>Datum narození</label>
                <input
                  type="date"
                  name="birthDate"
                  value={form.birthDate}
                  onChange={handleChange}
                />
              </div>

              <div className="form-row">
                <label>Pohlaví</label>
                <select name="gender" value={form.gender || ""} onChange={handleChange}>
                  <option value="">Nezadáno</option>
                  <option value="MALE">Muž</option>
                  <option value="FEMALE">Žena</option>
                  <option value="OTHER">Jiné</option>
                </select>
              </div>

              <div className="form-row">
                <label>E-mail</label>
                <input value={user.email} disabled />
              </div>

              <button type="submit" className="save-btn" disabled={loading}>
                {loading ? "Ukládám..." : "Uložit změny"}
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default ProfilePage;
