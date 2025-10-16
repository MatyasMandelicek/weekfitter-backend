import React, { useState } from "react";

function EventForm() {
  const [event, setEvent] = useState({
    title: "",
    description: "",
    startTime: "",
    endTime: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setEvent((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    fetch("http://localhost:8080/api/events", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ...event,
        user: { id: 1 }, // TODO: později nahradíme reálným přihlášeným uživatelem
      }),
    })
      .then((res) => res.json())
      .then((data) => {
        alert("Událost přidána!");
        console.log("Created:", data);
      })
      .catch((err) => console.error("Error creating event:", err));
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 bg-white rounded shadow-md">
      <h2 className="text-xl font-semibold mb-4">Přidat novou událost</h2>

      <input
        type="text"
        name="title"
        placeholder="Název"
        value={event.title}
        onChange={handleChange}
        className="block w-full p-2 mb-3 border rounded"
        required
      />

      <textarea
        name="description"
        placeholder="Popis"
        value={event.description}
        onChange={handleChange}
        className="block w-full p-2 mb-3 border rounded"
      />

      <label>Začátek:</label>
      <input
        type="datetime-local"
        name="startTime"
        value={event.startTime}
        onChange={handleChange}
        className="block w-full p-2 mb-3 border rounded"
        required
      />

      <label>Konec:</label>
      <input
        type="datetime-local"
        name="endTime"
        value={event.endTime}
        onChange={handleChange}
        className="block w-full p-2 mb-3 border rounded"
        required
      />

      <button
        type="submit"
        className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
      >
        Uložit událost
      </button>
    </form>
  );
}

export default EventForm;
