import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import withDragAndDrop from "react-big-calendar/lib/addons/dragAndDrop";
import "react-big-calendar/lib/addons/dragAndDrop/styles.css";
import { format, parse, startOfWeek, getDay, addMinutes, startOfMonth, endOfMonth, addDays, isWithinInterval } from "date-fns";
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
  const [view, setView] = useState(Views.MONTH);
  const [date, setDate] = useState(new Date());
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

  // === Barvy událostí ===
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

  // === Custom zobrazení události ===
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

  // === Synchronizace výšek týdnů ===
  useEffect(() => {
    const syncHeights = () => {
      const weekRows = document.querySelectorAll(".rbc-month-row");
      const weekBlocks = document.querySelectorAll(".week-block");

      if (weekRows.length === weekBlocks.length) {
        weekRows.forEach((row, i) => {
          weekBlocks[i].style.height = `${row.offsetHeight}px`;
        });
      }
    };

    syncHeights();
    window.addEventListener("resize", syncHeights);

    const observer = new MutationObserver(syncHeights);
    const calendar = document.querySelector(".rbc-month-view");
    if (calendar) observer.observe(calendar, { childList: true, subtree: true });

    return () => {
      window.removeEventListener("resize", syncHeights);
      observer.disconnect();
    };
  }, [events, view]);

  // === Přetažení události (drag & drop) ===
  const handleEventDrop = async ({ event, start, end }) => {
    const localStart = new Date(start.getTime() - start.getTimezoneOffset() * 60000);
    const localEnd = new Date(end.getTime() - end.getTimezoneOffset() * 60000);
    const updatedEvent = { ...event, startTime: localStart.toISOString(), endTime: localEnd.toISOString() };
    await fetch(`http://localhost:8080/api/events/${event.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedEvent),
    });
    await loadEvents();
  };

  const handleEventResize = handleEventDrop;

  // === Kliknutí na buňku (nová událost) ===
  const handleSelectSlot = (slotInfo) => {
    let start = slotInfo.start;
    let end;
    if (view === "month") {
      start = new Date(start.getFullYear(), start.getMonth(), start.getDate(), 8, 0);
      end = new Date(start.getFullYear(), start.getMonth(), start.getDate(), 8, 30);
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

  // === Odeslání formuláře ===
  const handleSubmit = async (e) => {
    e.preventDefault();
    let uploadedFilePath = formData.filePath;
    if (formData.file) {
      const uploadData = new FormData();
      uploadData.append("file", formData.file);
      const uploadRes = await fetch("http://localhost:8080/api/files/upload", { method: "POST", body: uploadData });
      uploadedFilePath = await uploadRes.text();
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
    await fetch(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
    setShowModal(false);
    setSelectedEvent(null);
    await loadEvents();
  };

  // === Vypočet týdenních souhrnů ===
  const getWeeklySummaries = () => {
    const start = startOfMonth(date);
    const end = endOfMonth(date);
    let weeks = [];
    for (let i = 0; i < 6; i++) {
      const weekStart = addDays(startOfWeek(addDays(start, i * 7), { weekStartsOn: 1 }), 0);
      const weekEnd = addDays(weekStart, 6);
      if (weekStart > end) break;

      const weekEvents = events.filter(
        (e) => e.category === "SPORT" && isWithinInterval(e.start, { start: weekStart, end: weekEnd })
      );

      const totals = { RUNNING: 0, CYCLING: 0, SWIMMING: 0, OTHER: 0 };
      weekEvents.forEach((ev) => {
        if (ev.sportType && totals.hasOwnProperty(ev.sportType)) {
          totals[ev.sportType] += ev.duration || 0;
        } else totals.OTHER += ev.duration || 0;
      });
      weeks.push({ weekStart, weekEnd, totals });
    }
    return weeks;
  };

  const weeklySummaries = getWeeklySummaries();

  // === MODAL ===
  const autoResize = (e) => {
    e.target.style.height = "auto";
    e.target.style.height = `${e.target.scrollHeight}px`;
  };

  return (
    <>
      <Header />
      <main className="calendar-container">
        <div className="calendar-grid">
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
              style={{ height: 650 }}
              messages={{
                next: "Další",
                previous: "Předchozí",
                today: "Dnes",
                month: "Měsíc",
                week: "Týden",
                day: "Den",
              }}
            />
          </div>

          {/* Souhrn sportů */}
          {view === "month" && (
            <div className="weekly-summary-grid">
              {weeklySummaries.map((week, i) => (
                <div key={i} className="week-block">
                  <h4>
                    {format(week.weekStart, "d.M.")} – {format(week.weekEnd, "d.M.")}
                  </h4>
                  {Object.keys(week.totals).map((sport) => (
                    <div key={sport} className="sport-item">
                      <img src={sportIcons[sport]} alt={sport} />
                      <span>
                        {Math.floor(week.totals[sport] / 60)}h {week.totals[sport] % 60}m
                      </span>
                    </div>
                  ))}
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </>
  );
};

export default CalendarPage;
