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
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    start: "",
    end: "",
    category: "sport", // přidána kategorie
  });

  // Načtení událostí z backendu
  useEffect(() => {
    fetch("http://localhost:8080/api/events")
      .then((res) => res.json())
      .then((data) => {
        const formatted = data.map((event) => ({
          id: event.id,
          title: event.title,
          start: new Date(event.startTime),
          end: new Date(event.endTime),
          description: event.description,
          category: event.category || "other",
        }));
        setEvents(formatted);
      })
      .catch((err) => console.error("Chyba při načítání událostí:", err));
  }, []);

  // Získání barvy podle typu
  const getEventStyle = (event) => {
    let bgColor = "#ff6a00";
    switch (event.category) {
      case "sport":
        bgColor = "#28a745";
        break;
      case "work":
        bgColor = "#007bff";
        break;
      case "school":
        bgColor = "#ffc107";
        break;
      case "rest":
        bgColor = "#6f42c1";
        break;
      default:
        bgColor = "#ff6a00";
    }
    return {
      style: {
        backgroundColor: bgColor,
        borderRadius: "8px",
        color: "white",
        border: "none",
      },
    };
  };

  // Přidání / úprava události
  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      title: formData.title,
      description: formData.description,
      startTime: formData.start,
      endTime: formData.end,
      category: formData.category,
    };

    const method = selectedEvent ? "PUT" : "POST";
    const url = selectedEvent
      ? `http://localhost:8080/api/events/${selectedEvent.id}`
      : "http://localhost:8080/api/events";

    await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    setFormData({
      title: "",
      description: "",
      start: "",
      end: "",
      category: "sport",
    });
    setSelectedEvent(null);
    window.location.reload(); // refresh (jednoduché řešení)
  };

  // Smazání události
  const handleDelete = async () => {
    if (!selectedEvent) return;
    await fetch(`http://localhost:8080/api/events/${selectedEvent.id}`, {
      method: "DELETE",
    });
    setSelectedEvent(null);
    window.location.reload();
  };

  // Při kliknutí na událost – naplní formulář
  const handleSelectEvent = (event) => {
    setSelectedEvent(event);
    setFormData({
      title: event.title,
      description: event.description,
      start: event.start.toISOString().slice(0, 16),
      end: event.end.toISOString().slice(0, 16),
      category: event.category,
    });
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
            onSelectEvent={handleSelectEvent}
            eventPropGetter={getEventStyle}
          />

          {/* Formulář pro přidání / úpravu události */}
          <div className="event-form">
            <h3>{selectedEvent ? "Upravit událost" : "Přidat novou událost"}</h3>
            <form onSubmit={handleSubmit}>
              <input
                type="text"
                placeholder="Název"
                value={formData.title}
                onChange={(e) =>
                  setFormData({ ...formData, title: e.target.value })
                }
                required
              />
              <textarea
                placeholder="Popis"
                value={formData.description}
                onChange={(e) =>
                  setFormData({ ...formData, description: e.target.value })
                }
              />
              <label>Začátek:</label>
              <input
                type="datetime-local"
                value={formData.start}
                onChange={(e) =>
                  setFormData({ ...formData, start: e.target.value })
                }
                required
              />
              <label>Konec:</label>
              <input
                type="datetime-local"
                value={formData.end}
                onChange={(e) =>
                  setFormData({ ...formData, end: e.target.value })
                }
                required
              />

              <label>Kategorie:</label>
              <select
                value={formData.category}
                onChange={(e) =>
                  setFormData({ ...formData, category: e.target.value })
                }
              >
                <option value="sport">Sport</option>
                <option value="work">Práce</option>
                <option value="school">Škola</option>
                <option value="rest">Odpočinek</option>
                <option value="other">Jiné</option>
              </select>

              <button type="submit">
                {selectedEvent ? "Uložit změny" : "Přidat"}
              </button>
              {selectedEvent && (
                <button
                  type="button"
                  onClick={handleDelete}
                  className="delete-btn"
                >
                  Smazat
                </button>
              )}
            </form>
          </div>
        </div>
      </main>
    </>
  );
};

export default CalendarPage;
