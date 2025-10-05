import React from "react";
import { Link, useNavigate } from "react-router-dom";
import Logo from "../assets/Logo01.png";
import "../styles/Header.css";

const Header = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    console.log("Uživatel odhlášen");
    navigate("/");
  };

  return (
    <header className="header">
      <div className="header-content">
        <div className="logo-container" onClick={() => navigate("/")}>
          <img src={Logo} alt="WeekFitter Logo" className="header-logo" />
          <h2 className="app-name">WeekFitter</h2>
        </div>
        <nav className="nav-links">
          <Link to="/">Domů</Link>
          <Link to="/plan">Plán</Link>
          <button onClick={handleLogout} className="logout-btn">
            Odhlásit se
          </button>
        </nav>
      </div>
    </header>
  );
};

export default Header;
