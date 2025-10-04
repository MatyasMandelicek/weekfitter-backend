// src/components/Header.jsx
import React from "react";
import "./Header.css";
import logo from "C:\Škola\JCU\Bakalářská práce\WeekFitter\weekfitter-frontend\src\assets\Logo01.png";

const Header = () => {
  return (
    <header className="header">
      <img src={logo} alt="Logo" className="logo" />
      <nav>
        <a href="/">Home</a>
        <a href="/login">Login</a>
        <a href="/plan">Plan</a>
      </nav>
    </header>
  );
};

export default Header;
