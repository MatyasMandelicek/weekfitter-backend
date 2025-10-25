import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import { format, parse, startOfWeek, getDay, setHours, setMinutes } from "date-fns";
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
    allDay: false,
  });

  const [view, setView] = useState(Views.MONTH);
  const [date, setDate] = useState(new Date());

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
          allDay: event.allDay || false,
        }));
        setEvents(formatted);
      })
      .catch((err) => console.error("Chyba při načítání událostí:", err));
  }, []);

  const getEventStyle = (event) => {
    const colors = {
      SPORT: "#28a745",
      WORK: "#007bff",
      SCHOOL: "#ffc107",
      REST: "#6f42c1",
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

  // 👉 Oprava: správný lokální defaultní čas 8:00–8:30
  const handleSelectSlot = (slotInfo) => {
  // Převod kliknutého dne na lokální datum (Praha)
  const clickedDate = new Date(slotInfo.start);
  const localDate = new Date(clickedDate.toLocaleString("en-US", { timeZone: "Europe/Prague" }));

  // Nastavení výchozích časů 8:00–8:30 pro daný den
  const defaultStart = new Date(localDate);
  defaultStart.setHours(8, 0, 0, 0);

  const defaultEnd = new Date(localDate);
  defaultEnd.setHours(8, 30, 0, 0);

    setSelectedEvent(null);
    setFormData({
      title: "",
      description: "",
      start: defaultStart.toISOString().slice(0, 16),
      end: defaultEnd.toISOString().slice(0, 16),
      category: "SPORT",
      allDay: false,
    });
    setShowModal(true);
  };

  const handleSelectEvent = (event) => {
    setSelectedEvent(event);
    setFormData({
      title: event.title,
      description: event.description || "",
      start: format(event.start, "yyyy-MM-dd'T'HH:mm"),
      end: format(event.end, "yyyy-MM-dd'T'HH:mm"),
      category: event.category || "OTHER",
      allDay: event.allDay || false,
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    let payload;

    if (formData.allDay) {
      // při celodenní události nastavíme půlnoc -> půlnoc následujícího dne
      const startDate = new Date(formData.start);
      const endDate = new Date(startDate);
      endDate.setDate(startDate.getDate() + 1);

      payload = {
        title: formData.title,
        description: formData.description,
        startTime: startDate.toISOString(),
        endTime: endDate.toISOString(),
        category: formData.category,
        allDay: true,
      };
    } else {
      payload = {
        title: formData.title,
        description: formData.description,
        startTime: formData.start,
        endTime: formData.end,
        category: formData.category,
        allDay: false,
      };
    }

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

          {showModal && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3>{selectedEvent ? "Upravit událost" : "Přidat novou událost"}</h3>
                <form onSubmit={handleSubmit}>
                  <label>Název:</label>
                  <input
                    type="text"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    required
                  />

                  <label>Popis:</label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  />

                  <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <input
                      type="checkbox"
                      checked={formData.allDay}
                      onChange={(e) => setFormData({ ...formData, allDay: e.target.checked })}
                    />
                    <label>Celý den</label>
                  </div>

                  {!formData.allDay && (
                    <>
                      <label>Začátek:</label>
                      <input
                        type="datetime-local"
                        value={formData.start}
                        onChange={(e) => setFormData({ ...formData, start: e.target.value })}
                        required
                      />

                      <label>Konec:</label>
                      <input
                        type="datetime-local"
                        value={formData.end}
                        onChange={(e) => setFormData({ ...formData, end: e.target.value })}
                        required
                      />
                    </>
                  )}

                  <label>Kategorie:</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  >
                    <option value="SPORT">Sport</option>
                    <option value="WORK">Práce</option>
                    <option value="SCHOOL">Škola</option>
                    <option value="REST">Odpočinek</option>
                    <option value="OTHER">Jiné</option>
                  </select>

                  <div className="modal-buttons">
                    <button type="submit">
                      {selectedEvent ? "Uložit změny" : "Přidat"}
                    </button>
                    {selectedEvent && (
                      <button type="button" className="delete-btn" onClick={handleDelete}>
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
