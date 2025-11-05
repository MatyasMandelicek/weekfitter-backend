/**
 * 
 * App.js
 * Hlavní komponenta aplikace WeekFitter
 * 
 * Spravuje směrování (routing) pomocí React Routeru
 * Odděluje veřejné (login, register) a chráněné stránky (dashboard, kalendář, profil)
 * Využívá komponentu <ProtectedRoute> pro kontrolu přihlášení uživatele
 */
import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
// s
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import CalendarPage from "./pages/CalendarPage";
import RegisterPage from "./pages/RegisterPage";
import ProtectedRoute from "./components/ProtectedRoute";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import ProfilePage from "./pages/ProfilePage";



function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />  

        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password/:token" element={<ResetPasswordPage />} />
  

        <Route path="/" element={<ProtectedRoute element={<HomePage />} />} />
        <Route path="/home" element={<ProtectedRoute element={<HomePage />} />} />
        <Route path="/calendar" element={<ProtectedRoute element={<CalendarPage />} />} />
        <Route path="/dashboard" element={<ProtectedRoute element={<DashboardPage />} />} />
        <Route path="/profile" element={<ProtectedRoute element={<ProfilePage />} />} />
        </Routes>
    </Router>
  );
}

export default App;
