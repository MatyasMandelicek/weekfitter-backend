/**
 * ProtectedRoute.js
 * Ochrana přístupu k soukromým stránkám
 * 
 * Kontroluje, zda je uživatel přihlášen
 * Pokud není, přesměruje ho na přihlašovací stránku
 */

import React from "react";
import { Navigate } from "react-router-dom";

const ProtectedRoute = ({ element }) => {
  const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";

  // Pokud není přihlášený, přesměruj ho na login
  return isLoggedIn ? element : <Navigate to="/login" replace />;
};

export default ProtectedRoute;
