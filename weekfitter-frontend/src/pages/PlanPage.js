import React from "react";
import Header from "../components/Header";
import { Link } from "react-router-dom";

function PlanPage() {
  return (
    <div className="p-8">
      <Header />
      <h1 className="text-3xl font-bold mb-4">Your Workout Plan</h1>
      <p>Here you will see your weekly workout plans.</p>
      <Link to="/" className="text-blue-500 mt-4 inline-block">Back to Home</Link>
    </div>
  );
}

export default PlanPage;
