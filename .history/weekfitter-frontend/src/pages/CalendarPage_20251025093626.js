import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import { format, parse, startOfWeek, getDay,es,
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
    duration: "",
    distance: "",
    sportDescription: "",
    gpxFile: null,
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
      allDay: event.allDay,
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
    const base = new Date(clicked.getFullYear(), clicked.getMonth(), clicked.getDate(), 8, 0);
    const end = new Date(clicked.getFullYear(), clicked.getMonth(), clicked.getDate(), 8, 30);
    setSelectedEvent(null);
    setFormData({
      ...formData,
      title: "",
      description: "",
      start: format(base, "yyyy-MM-dd'T'HH:mm"),
      end: format(end, "yyyy-MM-dd'T'HH:mm"),
      category: "SPORT",
      allDay: false,
      duration: "",
      distance: "",
      sportDescription: "",
      gpxFile: null,
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    let uploadedFilePath = null;
    if (formData.gpxFile) {
      const formDataFile = new FormData();
      formDataFile.append("file", formData.gpxFile);
      const uploadRes = await fetch("http://localhost:8080/api/files/upload", {
        method: "POST",
        body: formDataFile,
      });
      uploadedFilePath = await uploadRes.text();
    }

    const payload = {
      title: formData.title,
      description: formData.description,
      startTime: formData.start,
      endTime: formData.end,
      category: formData.category,
      duration: formData.category === "SPORT" ? formData.duration : null,
      distance: formData.category === "SPORT" ? formData.distance : null,
      sportDescription: formData.category === "SPORT" ? formData.sportDescription : null,
      gpxFilePath: uploadedFilePath,
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
                <h3>Přidat novou událost</h3>
                <form onSubmit={handleSubmit}>
                  <label>Název:</label>
                  <input
                    type="text"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    required
                  />

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

                  {formData.category === "SPORT" && (
                    <>
                      <hr />
                      <h4>Sportovní údaje</h4>
                      <label>Trvání (v minutách):</label>
                      <input
                        type="number"
                        value={formData.duration}
                        onChange={(e) => setFormData({ ...formData, duration: e.target.value })}
                      />
                      <label>Vzdálenost (km):</label>
                      <input
                        type="number"
                        value={formData.distance}
                        onChange={(e) => setFormData({ ...formData, distance: e.target.value })}
                      />
                      <label>Popis aktivity:</label>
                      <textarea
                        value={formData.sportDescription}
                        onChange={(e) =>
                          setFormData({ ...formData, sportDescription: e.target.value })
                        }
                      />
                      <label>Soubor GPX/JSON:</label>
                      <input
                        type="file"
                        accept=".gpx,.json"
                        onChange={(e) => setFormData({ ...formData, gpxFile: e.target.files[0] })}
                      />
                    </>
                  )}

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

                  <label>Popis:</label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  />

                  <div className="modal-buttons">
                    <button type="submit">Uložit</button>
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
