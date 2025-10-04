import React from "react";
import Header from "../components/Header";
import { Link } from "react-router-dom";

function LoginPage() {
  return (
    <div className="p-8">
      <Header />
      <h1 className="text-3xl font-bold mb-4">Login</h1>
      <form className="flex flex-col max-w-sm">
        <input type="text" placeholder="Username" className="mb-2 p-2 border"/>
        <input type="password" placeholder="Password" className="mb-2 p-2 border"/>
        <button type="submit" className="bg-blue-500 text-white p-2">Login</button>
      </form>
      <Link to="/" className="text-blue-500 mt-4 inline-block">Back to Home</Link>
    </div>
  );
}

export default LoginPage;
