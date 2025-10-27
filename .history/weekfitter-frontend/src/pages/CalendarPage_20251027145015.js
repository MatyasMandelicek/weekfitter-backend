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

import runIcon from "../assets/icons/run.png";
import bikeIcon from "../assets/icons/bike.png";
import swimIcon from "../assets/icons/swim.png";
import otherIcon from "../assets/icons/other.png";

const locales = { cs };
const localizer = dateFnsLocalizer({
  format,
  parse: (value, fmt) => parse(value, fmt, new Date(), { locale: cs }),
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

  const loadEvents = async () => {
    const email = localStorage.getItem("userEmail");
    if (!email) {
      setEvents([]);
      return;
    }
    try {
      const res = await fetch(`http://localhost:8080/api/events?email=${encodeURIComponent(email)}`);
      const data = await res.json();
      if (!Array.isArray(data)) {
        setEvents([]);
        return;
      }
      const formatted = data.map((event) => ({
        id: event.id,
        title: event.title,
        start: new Date(event.startTime),
        end: new Date(event.endTime),
        description: event.description,
        category: event.category ?? "OTHER",
        allDay: Boolean(event.allDay),
        duration: event.duration,
        distance: event.distance,
        sportDescription: event.sportDescription,
        sportType: event.sportType ?? "OTHER",
        filePath: event.filePath,
      }));
      setEvents(formatted);
    } catch (error) {
      console.error("Chyba při načítání událostí:", error);
    }
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

  const CustomEvent = ({ event }) => {
    if (event.category === "SPORT") {
      const iconSrc = sportIcons[event.sportType] || sportIcons.OTHER;
      return (
        <div className="custom-event">
          <img src={iconSrc} alt={event.sportType || "SPORT"} className="event-icon-img" />
          <span className="event-title">{event.title}</span>
        </div>
      );
    }
    return <div className="event-title">{event.title}</div>;
  };

  const autoResize = (e) => {
    e.target.style.height = "auto";
    e.target.style.height = `${e.target.scrollHeight}px`;
  };

  const handleSelectSlot = (slotInfo) => {
    let start = slotInfo.start;
    let end;
    if (view === Views.MONTH) {
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

  const handleSelectEvent = (event) => {
    setSelectedEvent(event);
    setFormData({
      title: event.title,
      description: event.description,
      start: format(event.start, "yyyy-MM-dd'T'HH:mm"),
      end: format(event.end, "yyyy-MM-dd'T'HH:mm"),
      category: event.category,
      allDay: Boolean(event.allDay),
      duration: event.duration || "",
      distance: event.distance || "",
      sportDescription: event.sportDescription || "",
      sportType: event.sportType || "OTHER",
      file: null,
      filePath: event.filePath || null,
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
    const minutes = parseInt(e.target.value, 10);
    if (!isNaN(minutes) && formData.start) {
      const startDate = new Date(formData.start);
      const newEnd = addMinutes(startDate, minutes);
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
    const newStart = new Date(e.target.value);
    if (formData.duration && !isNaN(parseInt(formData.duration, 10))) {
      const minutes = parseInt(formData.duration, 10);
      const newEnd = addMinutes(newStart, minutes);
      setFormData((prev) => ({
        ...prev,
        start: format(newStart, "yyyy-MM-dd'T'HH:mm"),
        end: format(newEnd, "yyyy-MM-dd'T'HH:mm"),
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        start: format(newStart, "yyyy-MM-dd'T'HH:mm"),
      }));
    }
  };

  const buildPayloadFromEvent = (base, overrides = {}) => {
    const resolvedCategory = (overrides.category ?? base.category) || "OTHER";
    const isSport = resolvedCategory === "SPORT";
    const startVal = overrides.start ? new Date(overrides.start) : new Date(base.start);
    const endVal = overrides.end ? new Date(overrides.end) : new Date(base.end);
    const startTime = format(startVal, "yyyy-MM-dd'T'HH:mm");
    const endTime = format(endVal, "yyyy-MM-dd'T'HH:mm");
    return {
      id: base.id,
      title: overrides.title ?? base.title,
      description: isSport
        ? overrides.sportDescription ?? base.sportDescription ?? ""
        : overrides.description ?? base.description ?? "",
      startTime,
      endTime,
      category: resolvedCategory,
      allDay: isSport ? false : Boolean(overrides.allDay ?? base.allDay),
      duration: isSport ? Number(overrides.duration ?? base.duration ?? 0) || null : null,
      distance: isSport ? Number(overrides.distance ?? base.distance ?? 0) || null : null,
      sportDescription: isSport ? overrides.sportDescription ?? base.sportDescription ?? "" : null,
      sportType: isSport ? overrides.sportType ?? base.sportType ?? "OTHER" : null,
      filePath: overrides.filePath ?? base.filePath ?? null,
    };
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const email = localStorage.getItem("userEmail");
    if (!email) {
      alert("Uživatel není přihlášen.");
      return;
    }

    const payload = {
      title: formData.title,
      description: formData.category === "SPORT" ? formData.sportDescription : formData.description,
      startTime: formData.start,
      endTime: formData.end,
      category: formData.category,
      allDay: formData.category !== "SPORT" ? formData.allDay : false,
      duration: formData.category === "SPORT" ? (formData.duration ? Number(formData.duration) : null) : null,
      distance: formData.category === "SPORT" ? (formData.distance ? Number(formData.distance) : null) : null,
      sportDescription: formData.category === "SPORT" ? formData.sportDescription : null,
      sportType: formData.category === "SPORT" ? formData.sportType : null,
      filePath: formData.filePath || null,
    };

    const method = selectedEvent ? "PUT" : "POST";
    const url = selectedEvent
      ? `http://localhost:8080/api/events/${selectedEvent.id}?email=${encodeURIComponent(email)}`
      : `http://localhost:8080/api/events?email=${encodeURIComponent(email)}`;

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        alert("Chyba při ukládání události.");
        return;
      }

      setShowModal(false);
      setSelectedEvent(null);
      await loadEvents();
    } catch {
      alert("Chyba spojení s backendem.");
    }
  };

  const handleDelete = async () => {
    if (!selectedEvent) return;
    try {
      await fetch(`http://localhost:8080/api/events/${selectedEvent.id}`, { method: "DELETE" });
      setShowModal(false);
      setSelectedEvent(null);
      await loadEvents();
    } catch {
      alert("Chyba při mazání události.");
    }
  };

  const handleEventDrop = async ({ event, start, end }) => {
    const email = localStorage.getItem("userEmail");
    const payload = buildPayloadFromEvent(event, { start, end });

    setEvents((prev) =>
      prev.map((e) => (e.id === event.id ? { ...e, start: new Date(start), end: new Date(end) } : e))
    );

    await fetch(`http://localhost:8080/api/events/${event.id}?email=${encodeURIComponent(email)}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    await loadEvents();
  };

  const handleEventResize = handleEventDrop;

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
            onDoubleClickEvent={handleSelectEvent}
            longPressThreshold={50}
            popup
            eventPropGetter={getEventStyle}
            components={{ event: CustomEvent }}
            view={view}
            date={date}
            onView={setView}
            onNavigate={setDate}
            style={{ height: 750, fontSize: "0.95rem", touchAction: "manipulation" }}
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
        </div>
      </main>
    </>
  );
};

export default CalendarPage;
