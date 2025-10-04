// src/pages/HomePage.js
import React from "react";
import Header from "../components/Header";

const HomePage = () => {
  return (
    <>
      <Header />
      <main style={{ padding: "20px", textAlign: "center" }}>
        <h1>Vítejte ve WeekFitter!</h1>
        <p>Plánujte svůj sportovní týden jednoduše a přehledně.</p>
      </main>
    </>
  );
};

export default HomePage;
