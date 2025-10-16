import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import "../styles/ActivityPage.css";

const ActivityPage = () => {
  const [activities, setActivities] = useState([]);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    durationMinutes: "",
    distanceKm: "",
    caloriesBurned: "",
  });

  // Načtení aktivit po načtení stránky
  useEffect(() => {
    fetch("http://localhost:8080/api/activities")
      .then((res) => res.json())
      .then((data) => setActivities(data))
      .catch((err) => console.error("Chyba při načítání aktivit:", err));
  }, []);

  // Odeslání formuláře
  const handleSubmit = (e) => {
    e.preventDefault();

    fetch("http://localhost:8080/api/activities", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(formData),
    })
      .then((res) => res.json())
      .then((newActivity) => {
        setActivities([...activities, newActivity]);
        setFormData({
          name: "",
          description: "",
          durationMinutes: "",
          distanceKm: "",
          caloriesBurned: "",
        });
      })
      .catch((err) => console.error("Chyba při přidávání aktivity:", err));
  };

  // Změna ve formuláři
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <>
      <Header />
      <main className="activity-container">
        <div className="activity-card">
          <h2>Sportovní aktivity</h2>
          <form onSubmit={handleSubmit} className="activity-form">
            <input
              type="text"
              name="name"
              placeholder="Název aktivity"
              value={formData.name}
              onChange={handleChange}
              required
            />
            <textarea
              name="description"
              placeholder="Popis aktivity"
              value={formData.description}
              onChange={handleChange}
            ></textarea>
            <input
              type="number"
              name="durationMinutes"
              placeholder="Délka (minuty)"
              value={formData.durationMinutes}
              onChange={handleChange}
              required
            />
            <input
              type="number"
              name="distanceKm"
              placeholder="Vzdálenost (km)"
              value={formData.distanceKm}
              onChange={handleChange}
            />
            <input
              type="number"
              name="caloriesBurned"
              placeholder="Spálené kalorie"
              value={formData.caloriesBurned}
              onChange={handleChange}
            />
            <button type="submit">Přidat aktivitu</button>
          </form>

          <ul className="activity-list">
            {activities.length > 0 ? (
              activities.map((a) => (
                <li key={a.id}>
                  <strong>{a.name}</strong> — {a.durationMinutes} min,{" "}
                  {a.distanceKm} km, {a.caloriesBurned} kcal
                  <p>{a.description}</p>
                </li>
              ))
            ) : (
              <p className="no-activities">Zatím žádné aktivity.</p>
            )}
          </ul>
        </div>
      </main>
    </>
  );
};

export default ActivityPage;
