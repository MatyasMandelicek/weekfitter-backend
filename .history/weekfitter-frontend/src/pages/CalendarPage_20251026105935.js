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

// Ikony sportů
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

  // === Načtení událostí ===
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
      sportType: event.sportType,
      filePath: event.filePath,
    }));
    setEvents(formatted);
  };

  useEffect(() => {
    loadEvents();
  }, []);

  // === Styl událostí ===
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

  // === Událost s ikonou ===
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

  // === Nová událost ===
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

  // === Kliknutí na událost ===
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

  // === Uložení / úprava ===
  const handleSubmit = async (e) => {
    e.preventDefault();

    let uploadedFilePath = formData.filePath;
    if (formData.file) {
      const uploadData = new FormData();
      uploadData.append("file", formData.file);

      const uploadRes = await fetch("http://localhost:8080/api/files/upload", {
        method: "POST",
        body: uploadData,
      });
      if (uploadRes.ok) uploadedFilePath = await uploadRes.text();
    }

    const payload = {
      title: formData.title,
      description: formData.category === "SPORT" ? formData.sportDescription : formData.description,
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

  const handleEventDrop = async ({ event, start, end }) => {
    const updatedEvent = { ...event, startTime: start.toISOString(), endTime: end.toISOString() };
    await fetch(`http://localhost:8080/api/events/${event.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedEvent),
    });
    await loadEvents();
  };

  // === Statistická část — zarovnaná s týdny ===
  const renderWeeklyStats = () => {
    if (view !== "month") return null;

    const monthStart = startOfMonth(date);
    const monthEnd = endOfMonth(date);
    const weeks = eachWeekOfInterval({ start: monthStart, end: monthEnd }, { weekStartsOn: 1 });

    const toHours = (min) => {
      const safe = Number.isFinite(min) ? min : 0;
      const h = Math.floor(safe / 60);
      const m = safe % 60;
      return `${h}h ${m}m`;
    };

    return (
      <div className="calendar-grid">
        {weeks.map((weekStart, idx) => {
          const weekEnd = endOfWeek(weekStart, { weekStartsOn: 1 });

          const weekEvents = events.filter(
            (e) => e.category === "SPORT" && e.start >= weekStart && e.start <= weekEnd
          );

          const totals = { RUNNING: 0, CYCLING: 0, SWIMMING: 0, OTHER: 0 };
          weekEvents.forEach((e) => {
            const dur = e.duration || 0;
            const key =
              e.sportType && totals[e.sportType] !== undefined ? e.sportType : "OTHER";
            totals[key] += dur;
          });

          return (
            <div className="calendar-row" key={idx}>
              <div className="calendar-week">
                {idx === 0 && (
                  <DnDCalendar
                    localizer={localizer}
                    events={events}
                    startAccessor="start"
                    endAccessor="end"
                    selectable
                    resizable
                    onEventDrop={handleEventDrop}
                    onSelectSlot={handleSelectSlot}
                    onSelectEvent={handleSelectEvent}
                    eventPropGetter={getEventStyle}
                    components={{ event: CustomEvent }}
                    view={view}
                    date={date}
                    onView={setView}
                    onNavigate={setDate}
                    style={{ height: 150 * weeks.length }}
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
                )}
              </div>

              <div className="week-stats">
                <div className="week-range">
                  {format(weekStart, "d.M.")} – {format(weekEnd, "d.M.")}
                </div>
                <div className="sport-line">
                  <img src={runIcon} alt="běh" />
                  <span>{toHours(totals.RUNNING)}</span>
                </div>
                <div className="sport-line">
                  <img src={bikeIcon} alt="kolo" />
                  <span>{toHours(totals.CYCLING)}</span>
                </div>
                <div className="sport-line">
                  <img src={swimIcon} alt="plavání" />
                  <span>{toHours(totals.SWIMMING)}</span>
                </div>
                <div className="sport-line">
                  <img src={otherIcon} alt="jiné" />
                  <span>{toHours(totals.OTHER)}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  return (
    <>
      <Header />
      <main className="calendar-container">
        <div className="calendar-card">
          <h2>Kalendář aktivit</h2>
          {view === "month" ? (
            renderWeeklyStats()
          ) : (
            <DnDCalendar
              localizer={localizer}
              events={events}
              startAccessor="start"
              endAccessor="end"
              selectable
              resizable
              onEventDrop={handleEventDrop}
              onSelectSlot={handleSelectSlot}
              onSelectEvent={handleSelectEvent}
              eventPropGetter={getEventStyle}
              components={{ event: CustomEvent }}
              view={view}
              date={date}
              onView={setView}
              onNavigate={setDate}
              style={{ height: 800 }}
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
          )}
        </div>
      </main>
    </>
  );
};

export default CalendarPage;
