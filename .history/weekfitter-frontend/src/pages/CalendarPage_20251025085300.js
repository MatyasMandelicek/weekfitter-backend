import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import { format,
  parse,
  startOfWeek,
  getDay,
  setHours,
  setMinutes,
  isSameDay,
} from "date-fns";
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

  const loadEvents = async () => {
    const res = await fetch("http://localhost:8080/api/events");
    const data = await res.json();
    const formatted = data.map((event) => {
      const s = new Date(event.startTime);
      const e = new Date(event.endTime);
      const allDayComputed =
        isSameDay(s, e) &&
        s.getHours() === 0 &&
        s.getMinutes() === 0 &&
        e.getHours() === 23 &&
        e.getMinutes() === 59;

      return {
        id: event.id,
        title: event.title,
        start: s,
        end: e,
        description: event.description,
        category: event.category,
        allDay: allDayComputed,
      };
    });
    setEvents(formatted);
  };

  useEffect(() => {
    loadEvents();
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

  const handleSelectSlot = (slotInfo) => {
    const clicked = new Date(slotInfo.start);
    const dayBase = new Date(
      clicked.getFullYear(),
      clicked.getMonth(),
      clicked.getDate(),
      0,
      0,
      0,
      0
    );

    const defaultStart = setHours(setMinutes(dayBase, 0), 8);  // 08:00
    const defaultEnd = setHours(setMinutes(dayBase, 30), 8);   // 08:30

    setSelectedEvent(null);
    setFormData({
      title: "",
      description: "",
      start: format(defaultStart, "yyyy-MM-dd'T'HH:mm"),
      end: format(defaultEnd, "yyyy-MM-dd'T'HH:mm"),
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
      allDay: !!event.allDay,
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    let startStr = formData.start;
    let endStr = formData.end;

    if (formData.allDay) {
      const base = formData.start
        ? new Date(formData.start)
        : new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);

      const startDay = new Date(
        base.getFullYear(),
        base.getMonth(),
        base.getDate(),
        0,
        0,
        0,
        0
      );
      const endDay = new Date(
        base.getFullYear(),
        base.getMonth(),
        base.getDate(),
        23,
        59,
        0,
        0
      );

      startStr = format(startDay, "yyyy-MM-dd'T'HH:mm");
      endStr = format(endDay, "yyyy-MM-dd'T'HH:mm");
    }

    const payload = {
      title: formData.title,
      description: formData.description,
      startTime: startStr,
      endTime: endStr,
      category: formData.category,
      // záměrně NEposíláme allDay do backendu, pokud ho entita nemá
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
    await loadEvents();
  };

  const handleDelete = async () => {
    if (!selectedEvent) return;
    await fetch(`http://localhost:8080/api/events/${selectedEvent.id}`, {
      method: "DELETE",
    });
    setShowModal(false);
    setSelectedEvent(null);
    await loadEvents();
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
