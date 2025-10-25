import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import { format, parse, startOfWeek, getDay,} from "date-fns";
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
    category: "OTHER",
    allDay: false,
    duration: "",
    distance: "",
    sportDescription: "",
  });

  const [view, setView] = useState(Views.MONTH);
  const [date, setDate] = useState(new Date());

  const loadEvents = async () => {
    const res = await fetch("http://localhost:8080/api/events");
    const data = await res.json();
    const formatted = data.map((event) => ({
      id: event.id,
      title: event.title,
      start: new Date(event.startTime),
      end: new Date(event.endTime),
      description: event.description,
      category: event.category,
      allDay: event.allDay || false,
      duration: event.duration,
      distance: event.distance,
      sportDescription: event.sportDescription,
    }));
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
    const dayStart = new Date(clicked.getFullYear(), clicked.getMonth(), clicked.getDate(), 8, 0);
    const dayEnd = new Date(clicked.getFullYear(), clicked.getMonth(), clicked.getDate(), 8, 30);

    setSelectedEvent(null);
    setFormData({
      title: "",
      description: "",
      start: format(dayStart, "yyyy-MM-dd'T'HH:mm"),
      end: format(dayEnd, "yyyy-MM-dd'T'HH:mm"),
      category: "OTHER",
      allDay: false,
      duration: "",
      distance: "",
      sportDescription: "",
    });
    setShowModal(true);
  };

  const handleCategoryChange = (e) => {
    const category = e.target.value;
    setFormData((prev) => ({
      ...prev,
      category,
      allDay: category === "SPORT" ? false : prev.allDay,
    }));
  };

  const handleDurationChange = (e) => {
    const minutes = parseInt(e.target.value);
    if (!isNaN(minutes) && formData.start) {
      const startDate = new Date(formData.start);
      const newEnd = new Date(startDate.getTime() + minutes * 60000);
      setFormData((prev) => ({
        ...prev,
        duration: e.target.value,
        end: format(newEnd, "yyyy-MM-dd'T'HH:mm"),
      }));
    } else {
      setFormData((prev) => ({ ...prev, duration: e.target.value }));
    }
  };

  const handleStartChange = (e) => {
    const newStart = e.target.value;
    const minutes = parseInt(formData.duration);
    if (!isNaN(minutes)) {
      const startDate = new Date(newStart);
      const newEnd = new Date(startDate.getTime() + minutes * 60000);
      setFormData((prev) => ({
        ...prev,
        start: newStart,
        end: format(newEnd, "yyyy-MM-dd'T'HH:mm"),
      }));
    } else {
      setFormData((prev) => ({ ...prev, start: newStart }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    let startDate = new Date(formData.start);
    let endDate = new Date(formData.end);

    if (formData.category !== "SPORT" && formData.allDay) {
      startDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate(), 0, 0, 0, 0);
      endDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate(), 23, 59, 0, 0);
    } else {
      if (endDate < startDate) {
        const minutes = parseInt(formData.duration);
        endDate = !isNaN(minutes)
          ? new Date(startDate.getTime() + minutes * 60000)
          : new Date(startDate.getTime() + 30 * 60000);
      }
    }

    const payload = {
      title: formData.title,
      description: formData.category === "SPORT" ? formData.sportDescription : formData.description,
      startTime: format(startDate, "yyyy-MM-dd'T'HH:mm"),
      endTime: format(endDate, "yyyy-MM-dd'T'HH:mm"),
      category: formData.category || "OTHER",
      allDay: formData.category !== "SPORT" ? formData.allDay : false,
      duration: formData.category === "SPORT" && formData.duration ? Number(formData.duration) : null,
      distance: formData.category === "SPORT" && formData.distance ? Number(formData.distance) : null,
      sportDescription: formData.category === "SPORT" ? (formData.sportDescription || "") : null,
    };

    const method = selectedEvent ? "PUT" : "POST";
    const url = selectedEvent
      ? `http://localhost:8080/api/events/${selectedEvent.id}`
      : "http://localhost:8080/api/events";

    const res = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const text = await res.text();
      alert("Chyba při ukládání události: " + text);
      return;
    }

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
            eventPropGetter={getEventStyle}
            view={view}
            date={date}
            onView={setView}
            onNavigate={setDate}
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

                  <label>Kategorie:</label>
                  <select value={formData.category} onChange={handleCategoryChange}>
                    <option value="SPORT">Sport</option>
                    <option value="WORK">Práce</option>
                    <option value="SCHOOL">Škola</option>
                    <option value="REST">Odpočinek</option>
                    <option value="OTHER">Jiné</option>
                  </select>

                  {formData.category === "SPORT" ? (
                    <div className="sport-section">
                      <h4>Sportovní údaje</h4>
                      <label>Popis aktivity:</label>
                      <textarea
                        value={formData.sportDescription}
                        onChange={(e) =>
                          setFormData({ ...formData, sportDescription: e.target.value })
                        }
                      />
                      <label>Trvání (minuty):</label>
                      <input
                        type="number"
                        value={formData.duration}
                        onChange={handleDurationChange}
                      />
                      <label>Vzdálenost (km):</label>
                      <input
                        type="number"
                        value={formData.distance}
                        onChange={(e) =>
                          setFormData({ ...formData, distance: e.target.value })
                        }
                      />
                    </div>
                  ) : (
                    <>
                      <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                        <input
                          type="checkbox"
                          checked={formData.allDay}
                          onChange={(e) =>
                            setFormData({ ...formData, allDay: e.target.checked })
                          }
                        />
                        <label>Celý den</label>
                      </div>

                      <label>Popis:</label>
                      <textarea
                        value={formData.description}
                        onChange={(e) =>
                          setFormData({ ...formData, description: e.target.value })
                        }
                      />
                    </>
                  )}

                  {!formData.allDay && (
                    <>
                      <label>Začátek:</label>
                      <input
                        type="datetime-local"
                        value={formData.start}
                        onChange={handleStartChange}
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

                  <div className="modal-buttons">
                    <button type="submit">{selectedEvent ? "Uložit" : "Přidat"}</button>
                    <button
                      type="button"
                      className="cancel-btn"
                      onClick={() => setShowModal(false)}
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
