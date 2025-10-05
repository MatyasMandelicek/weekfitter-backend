import React from "react";
import Header from "../components/Header";
import "../styles/PlanPage.css";

const days = [
  { day: "Pondělí", activity: "Běh - 5 km", duration: "30 min" },
  { day: "Úterý", activity: "Posilování - Core", duration: "45 min" },
  { day: "Středa", activity: "Cyklistika - 20 km", duration: "60 min" },
  { day: "Čtvrtek", activity: "Volno", duration: "-" },
  { day: "Pátek", activity: "Plavání - 1 km", duration: "40 min" },
  { day: "Sobota", activity: "Běh - 10 km", duration: "60 min" },
  { day: "Neděle", activity: "Regenerace", duration: "-" },
];

const PlanPage = () => {
  return (
    <>
      <Header />
      <main className="plan-page">
        <h1>Týdenní plán tréninků</h1>
        <div className="plan-grid">
          {days.map((item, index) => (
            <div key={index} className="plan-card">
              <h2>{item.day}</h2>
              <p>{item.activity}</p>
              <p className="duration">{item.duration}</p>
            </div>
          ))}
        </div>
      </main>
    </>
  );
};

export default PlanPage;
