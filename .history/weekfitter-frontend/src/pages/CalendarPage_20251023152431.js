import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import { format, parse, startOfWeek, getDay } from "date-fns";
import { cs } from "date-fns/locale";
import "react-big-calendar/lib/css/react-big-calendar.css";
import Header from "../components/Header";
import "../styles/CalendarPage.css";

const locales = { cs };
const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek: () => startOfWeek(new Date(), { weekStartsOn: 1 }),
  getDay,
  locales,
});

const CalendarPage = () => {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);

  // Načtení událostí z backendu
  const fetchEvents = () => {
    fetch("http://localhost:8080/api/events")
      .then((res) => res.json())
      .then((data) => {
        const formatted = data.map((event) => ({
          id: event.id,
          title: event.title,
          start: new Date(event.startTime),
          end: new Date(event.endTime),
          description: event.description,
          activityType: event.activityType,
        }));
        setEvents(formatted);
      })
      .catch((err) => console.error("Chyba při načítání událostí:", err));
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  // Přidání události kliknutím
  const handleSelectSlot = ({ start, end }) => {
    const title = prompt("Název události:");
    if (title) {
      const activityType = prompt("Typ aktivity (SPORT, WORK, SCHOOL, REST):");
      const newEvent = {
        title,
        description: "",
        startTime: start,
        endTime: end,
        activityType: activityType || "OTHER",
      };

      fetch("http://localhost:8080/api/events", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newEvent),
      })
        .then(() => fetchEvents())
        .catch((err) => console.error("Chyba při vytváření události:", err));
    }
  };

  // Kliknutí na existující událost
  const handleSelectEvent = (event) => {
    const action = window.confirm(
      `Událost: ${event.title}\nPopis: ${event.description || "—"}\n\nChceš ji smazat?`
    );
    if (action) {
      fetch(`http://localhost:8080/api/events/${event.id}`, {
        method: "DELETE",
      })
        .then(() => fetchEvents())
        .catch((err) => console.error("Chyba při mazání události:", err));
    }
  };

  // Barvení podle typu aktivity
  const eventStyleGetter = (event) => {
    let backgroundColor = "#ff6a00";
    switch (event.activityType) {
      case "SPORT":
        backgroundColor = "#28a745";
        break;
      case "WORK":
        backgroundColor = "#007bff";
        break;
      case "SCHOOL":
        backgroundColor = "#ffc107";
        break;
      case "REST":
        backgroundColor = "#6f42c1";
        break;
      default:
        backgroundColor = "#ff6a00";
    }
    return {
      style: {
        backgroundColor,
        borderRadius: "8px",
        color: "white",
        border: "none",
      },
    };
  };

  return (
    <>
      <Header />
      <main className="calendar-container">
        <div className="calendar-card">
          <h2>Kalendář aktivit</h2>
          <Calendar
            localizer={localizer}
            events={events}
            startAccessor="start"
            endAccessor="end"
            selectable
            onSelectSlot={handleSelectSlot}
            onSelectEvent={handleSelectEvent}
            eventPropGetter={eventStyleGetter}
            style={{ height: 600 }}
            messages={{
              next: "Další",
              previous: "Předchozí",
              today: "Dnes",
              month: "Měsíc",
              week: "Týden",
              day: "Den",
            }}
            popup
          />
        </div>
      </main>
    </>
  );
};

export default CalendarPage;
