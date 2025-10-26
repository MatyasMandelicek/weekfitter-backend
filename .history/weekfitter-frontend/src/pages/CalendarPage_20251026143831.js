import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import withDragAndDrop from "react-big-calendar/lib/addons/dragAndDrop";
import "react-big-calendar/lib/addons/dragAndDrop/styles.css";
import {
  format,
  parse,
  startOfWeek,
  endOfWeek,
  startOfMonth,
  endOfMonth,
  eachWeekOfInterval,
  getDay,
  addMinutes,
} from "date-fns";
import { cs } from "date-fns/locale";
import "react-big-calendar/lib/css/react-big-calendar.css";
import Header from "../components/Header";
import "../styles/CalendarPage.css";

// Import ikon sportů
import runIcon from "../assets/icons/run.png";
import bikeIcon from "../assets/icons/bike.png";
import swimIcon from "../assets/icons/swim.png";
import otherIcon from "../assets/icons/other.png";

const locales = { cs };
const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek: () => startOfWeek(new Date(), { weekStartsOn: 1 }),
  getDay,
  locales,
});
const DnDCalendar = withDragAndDrop(Calendar);

const sportIcons = {
  RUNNING: runIcon,
  CYCLING: bikeIcon,
  SWIMMING: swimIcon,
  OTHER: otherIcon,
};

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
    sportType: "OTHER",
    file: null,
    filePath: null,
  });

  const [view, setView] = useState(Views.MONTH);
  const [date, setDate] = useState(new Date());

  // === ✅ Načtení událostí pouze pro přihlášeného uživatele ===
  const loadEvents = async () => {
    const email = localStorage.getItem("userEmail");
    if (!email) {
      console.error("Uživatel není přihlášen – chybí e-mail v localStorage.");
      setEvents([]);
      return;
    }

    try {
      const res = await fetch(`http://localhost:8080/api/events?email=${email}`);
      const data = await res.json();

      if (!Array.isArray(data)) {
        console.error("Server nevrátil pole událostí:", data);
        setEvents([]);
        return;
      }

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
        sportType: event.sportType,
        filePath: event.filePath,
      }));

      setEvents(formatted);
    } catch (error) {
      console.error("Chyba při načítání událostí:", error);
      setEvents([]);
    }
  };

  useEffect(() => {
    loadEvents();
  }, []);

  // === Barvy podle typu ===
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

  // === Vlastní zobrazení události s ikonou ===
  const CustomEvent = ({ event }) => {
    if (event.category === "SPORT") {
      const iconSrc = sportIcons[event.sportType] || sportIcons.OTHER;
      return (
        <div className="custom-event">
          <img src={iconSrc} alt={event.sportType} className="event-icon-img" />
          <span className="event-title">{event.title}</span>
        </div>
      );
    }
    return <div className="event-title">{event.title}</div>;
  };

  // === Kliknutí do buňky kalendáře ===
  const handleSelectSlot = (slotInfo) => {
    let start = slotInfo.start;
    let end;

    if (view === "month") {
      start = new Date(start.getFullYear(), start.getMonth(), start.getDate(), 8, 0, 0);
      end = new Date(start.getFullYear(), start.getMonth(), start.getDate(), 8, 30, 0);
    } else {
      end = addMinutes(start, 30);
    }

    setSelectedEvent(null);
    setFormData({
      title: "",
      description: "",
      start: format(start, "yyyy-MM-dd'T'HH:mm"),
      end: format(end, "yyyy-MM-dd'T'HH:mm"),
      category: "OTHER",
      allDay: false,
      duration: "",
      distance: "",
      sportDescription: "",
      sportType: "OTHER",
      file: null,
      filePath: null,
    });
    setShowModal(true);
  };

  // === Kliknutí na existující událost ===
  const handleSelectEvent = (event) => {
    setSelectedEvent(event);
    setFormData({
      title: event.title,
      description: event.description,
      start: format(event.start, "yyyy-MM-dd'T'HH:mm"),
      end: format(event.end, "yyyy-MM-dd'T'HH:mm"),
      category: event.category,
      allDay: event.allDay,
      duration: event.duration || "",
      distance: event.distance || "",
      sportDescription: event.sportDescription || "",
      sportType: event.sportType || "OTHER",
      file: null,
      filePath: event.filePath || null,
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const email = localStorage.getItem("userEmail");

    let uploadedFilePath = formData.filePath;
    if (formData.file) {
      const uploadData = new FormData();
      uploadData.append("file", formData.file);
      const uploadRes = await fetch("http://localhost:8080/api/files/upload", {
        method: "POST",
        body: uploadData,
      });
      if (!uploadRes.ok) {
        alert("Chyba při nahrávání souboru.");
        return;
      }
      uploadedFilePath = await uploadRes.text();
    }

    const payload = {
      title: formData.title,
      description:
        formData.category === "SPORT" ? formData.sportDescription : formData.description,
      startTime: formData.start,
      endTime: formData.end,
      category: formData.category,
      allDay: formData.category !== "SPORT" ? formData.allDay : false,
      duration: formData.category === "SPORT" ? Number(formData.duration) : null,
      distance: formData.category === "SPORT" ? Number(formData.distance) : null,
      sportDescription: formData.category === "SPORT" ? formData.sportDescription : null,
      sportType: formData.category === "SPORT" ? formData.sportType : null,
      filePath: uploadedFilePath,
    };

    const method = selectedEvent ? "PUT" : "POST";
    const url = selectedEvent
      ? `http://localhost:8080/api/events/${selectedEvent.id}`
      : `http://localhost:8080/api/events?email=${email}`;

    const res = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const msg = await res.text();
      alert("Chyba při ukládání události: " + msg);
      return;
    }

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

  // === Přesunutí a změna velikosti událostí ===
  const handleEventDrop = async ({ event, start, end }) => {
    const updatedEvent = { ...event, startTime: start, endTime: end };
    await fetch(`http://localhost:8080/api/events/${event.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedEvent),
    });
    await loadEvents();
  };

  const handleEventResize = async ({ event, start, end }) => {
    const updatedEvent = { ...event, startTime: start, endTime: end };
    await fetch(`http://localhost:8080/api/events/${event.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedEvent),
    });
    await loadEvents();
  };

  return (
    <>
      <Header />
      <main className="calendar-container">
        <div className="calendar-card">
          <h2>Kalendář aktivit</h2>
          <DnDCalendar
            localizer={localizer}
            events={events}
            startAccessor="start"
            endAccessor="end"
            selectable
            resizable
            onEventDrop={handleEventDrop}
            onEventResize={handleEventResize}
            onSelectSlot={handleSelectSlot}
            onSelectEvent={handleSelectEvent}
            eventPropGetter={getEventStyle}
            components={{ event: CustomEvent }}
            view={view}
            date={date}
            onView={setView}
            onNavigate={setDate}
            style={{ height: 750, fontSize: "0.95rem" }}
            messages={{
              next: "Další",
              previous: "Předchozí",
              today: "Dnes",
              month: "Měsíc",
              week: "Týden",
              day: "Den",
              agenda: "Agenda",
            }}
          />

          {/* Modal pro úpravu a přidání */}
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
                  <select value={formData.category} onChange={(e) => setFormData({ ...formData, category: e.target.value })}>
                    <option value="SPORT">Sport</option>
                    <option value="WORK">Práce</option>
                    <option value="SCHOOL">Škola</option>
                    <option value="REST">Odpočinek</option>
                    <option value="OTHER">Jiné</option>
                  </select>

                  <label>Popis:</label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  />

                  <div className="time-row">
                    <div>
                      <label>Začátek:</label>
                      <input
                        type="datetime-local"
                        value={formData.start}
                        onChange={(e) => setFormData({ ...formData, start: e.target.value })}
                        required
                      />
                    </div>
                    <div>
                      <label>Konec:</label>
                      <input
                        type="datetime-local"
                        value={formData.end}
                        onChange={(e) => setFormData({ ...formData, end: e.target.value })}
                        required
                      />
                    </div>
                  </div>

                  <div className="modal-buttons">
                    <button type="submit">{selectedEvent ? "Uložit" : "Přidat"}</button>
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
