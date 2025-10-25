import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
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
  const [showModal, setShowModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    start: "",
    end: "",
    category: "SPORT",
  });

  const [view, setView] = useState(Views.MONTH);
  const [date, setDate] = useState(new Date());

  // 🔹 Načtení událostí z backendu
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
          category: event.category,
        }));
        setEvents(formatted);
      })
      .catch((err) => console.error("Chyba při načítání událostí:", err));
  }, []);

  // Barvy podle typu aktivity
  const getEventStyle = (event) => {
    const colors = {
      SPORT: "#28a745",
      WORK: "#007bff",
      SCHOOL: "#ffc107",
      DAi: "#6f42c1",
      OTHER: "#ff6a00",
    };

    return {
      style: {
        backgroundColor: colors[event.category] || "#ff6a00",
        borderRadius: "8px",
        color: "white",
        border: "none",
        padding: "2px 4px",
      },
    };
  };

  // Kliknutí na volný slot → otevře formulář s přesným datem
  const handleSelectSlot = (slotInfo) => {
    const start = format(slotInfo.start, "yyyy-MM-dd'T'HH:mm");
    const end = format(slotInfo.end, "yyyy-MM-dd'T'HH:mm");

    setSelectedEvent(null);
    setFormData({
      title: "",
      description: "",
      start,
      end,
      category: "SPORT",
    });
    setShowModal(true);
  };

  // Kliknutí na existující událost → otevře pro úpravu
  const handleSelectEvent = (event) => {
    setSelectedEvent(event);
    setFormData({
      title: event.title,
      description: event.description || "",
      start: format(event.start, "yyyy-MM-dd'T'HH:mm"),
      end: format(event.end, "yyyy-MM-dd'T'HH:mm"),
      category: event.category || "OTHER",
    });
    setShowModal(true);
  };

  // Uložení (nové nebo upravené)
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

    setShowModal(false);
    setSelectedEvent(null);
    window.location.reload();
  };

  // Mazání události
  const handleDelete = async () => {
    if (!selectedEvent) return;
    await fetch(`http://localhost:8080/api/events/${selectedEvent.id}`, {
      method: "DELETE",
    });
    setShowModal(false);
    setSelectedEvent(null);
    window.location.reload();
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
            eventPropGetter={getEventStyle}
            view={view}
            date={date}
            onView={(newView) => setView(newView)}
            onNavigate={(newDate) => setDate(newDate)}
            style={{ height: 600 }}
            messages={{
              next: "Další",
              previous: "Předchozí",
              today: "Dnes",
              month: "Měsíc",
              week: "Týden",
              day: "Den",
            }}
          />

          {/* Modální okno */}
          {showModal && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3>
                  {selectedEvent ? "Upravit událost" : "Přidat novou událost"}
                </h3>
                <form onSubmit={handleSubmit}>
                  <label>Název:</label>
                  <input
                    type="text"
                    value={formData.title}
                    onChange={(e) =>
                      setFormData({ ...formData, title: e.target.value })
                    }
                    required
                  />

                  <label>Popis:</label>
                  <textarea
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
                    <option value="SPORT">Sport</option>
                    <option value="WORK">Práce</option>
                    <option value="SCHOOL">Škola</option>
                    <option value="DAILY">Odpočinek</option>
                    <option value="OTHER">Jiné</option>
                  </select>

                  <div className="modal-buttons">
                    <button type="submit">
                      {selectedEvent ? "Uložit změny" : "Přidat"}
                    </button>
                    {selectedEvent && (
                      <button
                        type="button"
                        className="delete-btn"
                        onClick={handleDelete}
                      >
                        Smazat
                      </button>
                    )}
                    <button
                      type="button"
                      className="cancel-btn"
                      onClick={() => {
                        setShowModal(false);
                        setSelectedEvent(null);
                      }}
                    >
                      Zrušit
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </div>
      </main>
    </>
  );
};

export default CalendarPage;
