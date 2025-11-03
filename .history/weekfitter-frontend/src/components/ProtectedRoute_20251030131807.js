import React from "react";
import { Navigate, Outlet } from "react-router-dom";

const ProtectedRoute = ({ element }) => {
  const isLoggedIn = () => localStorage.getItem("isLoggedIn") === "true";

  // Pokud není přihlášený, přesměruj ho na login
  return isLoggedIn ? <Outlet> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;
