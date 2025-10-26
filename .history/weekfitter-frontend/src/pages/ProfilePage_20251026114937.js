import React, { useEffect, useState, useCallback} from "react";
import Header from "../components/Header";
import "../styles/ProfilePage.css";

const ProfilePage = () => {
  const [user, setUser] = useState(null);
  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(false);
  const email = localStorage.getItem("userEmail");

  const loadProfile = useCallback(async () => {
    const res = await fetch(`http://localhost:8080/api/users/profile?email=${email}`);
    if (res.ok) {
      const data = await res.json();
      setUser(data);
      setForm({
        firstName: data.firstName || "",
        lastName: data.lastName || "",
        birthDate: data.birthDate || "",
      });
    }
  }; [email]);

  useEffect(() => {
    loadProfile();
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const res = await fetch(`http://localhost:8080/api/users/profile?email=${email}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(form),
    });
    if (res.ok) {
      alert("Profil uložen");
      loadProfile();
    } else {
      alert("Chyba při ukládání profilu.");
    }
    setLoading(false);
  };

  const handlePhotoUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append("file", file);
    setLoading(true);
    const res = await fetch(`http://localhost:8080/api/users/upload-photo?email=${email}`, {
      method: "POST",
      body: formData,
    });
    if (res.ok) {
      alert("Fotka nahrána");
      loadProfile();
    } else {
      alert("Chyba při nahrávání fotky.");
    }
    setLoading(false);
  };

  if (!user) return <div>Načítám profil...</div>;

  return (
    <>
      <Header />
      <div className="profile-container">
        <div className="profile-card">
          <h2>Můj profil</h2>

          <div className="profile-content">
            <div className="profile-photo-section">
              <img
                src={user.photo ? `http://localhost:8080${user.photo}` : "/default-avatar.png"}
                alt="Profil"
                className="profile-photo"
              />
              <input
                type="file"
                accept="image/*"
                onChange={handlePhotoUpload}
                disabled={loading}
              />
            </div>

            <form className="profile-form" onSubmit={handleSubmit}>
              <label>
                Jméno:
                <input
                  name="firstName"
                  value={form.firstName}
                  onChange={handleChange}
                  required
                />
              </label>
              <label>
                Příjmení:
                <input
                  name="lastName"
                  value={form.lastName}
                  onChange={handleChange}
                  required
                />
              </label>
              <label>
                Datum narození:
                <input
                  type="date"
                  name="birthDate"
                  value={form.birthDate}
                  onChange={handleChange}
                />
              </label>
              <label>
                E-mail:
                <input value={user.email} disabled />
              </label>

              <button type="submit" disabled={loading}>
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
