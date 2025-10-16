import React, { useEffect, useState } from "react";

function EventList() {
  const [events, setEvents] = useState([]);

  useEffect(() => {
    fetch("http://localhost:8080/api/events")
      .then((response) => response.json())
      .then((data) => setEvents(data))
      .catch((error) => console.error("Error fetching events:", error));
  }, []);

  return (
    <div className="p-6">
      <h2 className="text-2xl font-semibold mb-4">Kalendář událostí</h2>
      <table className="min-w-full border border-gray-300">
        <thead className="bg-gray-100">
          <tr>
            <th className="p-2 border">Název</th>
            <th className="p-2 border">Popis</th>
            <th className="p-2 border">Začátek</th>
            <th className="p-2 border">Konec</th>
          </tr>
        </thead>
        <tbody>
          {events.map((event) => (
            <tr key={event.id} className="hover:bg-gray-50">
              <td className="p-2 border">{event.title}</td>
              <td className="p-2 border">{event.description}</td>
              <td className="p-2 border">
                {new Date(event.startTime).toLocaleString()}
              </td>
              <td className="p-2 border">
                {new Date(event.endTime).toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default EventList;
