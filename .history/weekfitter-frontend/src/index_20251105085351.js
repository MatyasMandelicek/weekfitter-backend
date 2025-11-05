/**
 * index.js
 * Vstupní bod React aplikace WeekFitter
 * 
 * Vytváří kořen aplikace (root element)
 * Zapouzdřuje aplikaci do React.StrictMode
 * Načítá hlavní komponentu <App />
 * 
 */

import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css"; // Import globálních stylů

/**
 * Vytvoření "root" elementu, který React používá pro render celé aplikace.
 * index.html obsahuje <div id="root"></div>, sem se vloží App.
 */
const root = ReactDOM.createRoot(document.getElementById("root"));

root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
