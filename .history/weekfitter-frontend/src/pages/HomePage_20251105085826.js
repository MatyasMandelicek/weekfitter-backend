/**
 * HomePage.js
 * Domovská stránka po přihlášení
 * 
 * Zobrazuje logo a úvodní sdělení aplikace
 * Nabízí tlačítko „Začít plánovat“ – přesměrovává
 *  na kalendář nebo login podle přihlášení
 */

import React, { useEffect } from "react";
import Header from "../components/Header";
import Logo from "../assets/Logo01.png";
import { useNavigate } from "react-router-dom";
import "../styles/HomePage.css";

const HomePage = () => {
  const navigate = useNavigate();

  // Při načtení stránky ověří, zda je uživatel přihlášen
  useEffect(() => {
    const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";
    if (!isLoggedIn) navigate("/login");    
  }, [navigate]);

  // Kliknutím na tlačítko přesměruje podle přihlášení
  const handleStartClick = () => {
    const isLoggedIn =
      localStorage.getItem("isLoggedIn") === "true" ||
      localStorage.getItem("userEmail") !== null;

    navigate(isLoggedIn ? "/calendar" : "/login");
      navigate("/calendar"); //přihlášený uživatel - kalendář
    } else {
      navigate("/login"); //nepřihlášený - login
    }
  };

  return (
    <>
      <Header />
      <main className="home-container">
        <img src={Logo} alt="WeekFitter Logo" className="home-logo" />
        <h1>Vítejte ve WeekFitter</h1>
        <p>Plánujte svůj sportovní týden jednoduše a přehledně.</p>

        <button className="home-btn" onClick={handleStartClick}>
          Začít plánovat
        </button>
      </main>
    </>
  );
};

export default HomePage;
