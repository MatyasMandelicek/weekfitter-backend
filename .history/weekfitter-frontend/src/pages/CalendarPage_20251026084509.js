import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import withDragAndDrop from "react-big-calendar/lib/addons/dragAndDrop";
import "react-big-calendar/lib/addons/dragAndDrop/styles.css";
import {
  format,
  parse,
  startOfWeek,
  getDay,
  addMinutes,
  startOfMonth,
  endOfMonth,
  addDays,
  isWithinInterval,
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

  // Načtení událostí
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

  // Barvy událostí
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

  // Vlastní zobrazení s ikonou
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

  // Synchronizace výšek týdnů mezi kalendářem a souhrnem
  useEffect(() => {
    const syncHeights = () => {
      const weekRows = document.querySelectorAll(".rbc-month-row");
      const weekBlocks = document.querySelectorAll(".week-block");
      if (!weekRows.length || !weekBlocks.length) return;

      // vyrovnat počet (měsíc může mít 4–6 řádků)
      const n = Math.min(weekRows.length, weekBlocks.length);
      for (let i = 0; i < n; i++) {
        // +1 px na hraniční linku v rbc
        weekBlocks[i].style.height = `${weekRows[i].offsetHeight}px`;
      }
      // schovat přebytečné bloky, pokud měsíc má méně než 6 týdnů
      for (let i = n; i < weekBlocks.length; i++) {
        weekBlocks[i].style.height = "0px";
        weekBlocks[i].style.padding = "0";
        weekBlocks[i].style.borderTop = "none";
      }
    };

    const runLater = () => setTimeout(syncHeights, 0);
    runLater();
    window.addEventListener("resize", runLater);

    const calendar = document.querySelector(".rbc-month-view");
    const observer = new MutationObserver(runLater);
    if (calendar) observer.observe(calendar, { childList: true, subtree: true });

    return () => {
      window.removeEventListener("resize", runLater);
      observer.disconnect();
    };
  }, [events, view, date]);

  // Přetažení a změna délky
  const handleEventDrop = async ({ event, start, end }) => {
    const localStart = new Date(start.getTime() - start.getTimezoneOffset() * 60000);
    const localEnd = new Date(end.getTime() - end.getTimezoneOffset() * 60000);
    const updatedEvent = {
      ...event,
      startTime: localStart.toISOString(),
      endTime: localEnd.toISOString(),
    };
    await fetch(`http://localhost:8080/api/events/${event.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedEvent),
    });
    await loadEvents();
  };
  const handleEventResize = handleEventDrop;

  // Klik na slot (nová událost)
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

  // Klik na událost (edit)
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

  const handleCategoryChange = (e) => {
    const category = e.target.value;
    setFormData((prev) => ({
      ...prev,
      category,
      allDay: category === "SPORT" ? false : prev.allDay,
    }));
  };

  // Trvání → spočítá konec
  const handleDurationChange = (e) => {
    const minutes = parseInt(e.target.value);
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

  // Změna začátku → posune konec, pokud je vyplněné trvání
  const handleStartChange = (e) => {
    const newStart = new Date(e.target.value);
    if (formData.duration && !isNaN(parseInt(formData.duration))) {
      const minutes = parseInt(formData.duration);
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

  // Odeslání
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
      if (!uploadRes.ok) {
        const msg = await uploadRes.text();
        alert("Chyba při nahrávání souboru: " + msg);
        return;
      }
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

  // Smazání
  const handleDelete = async () => {
    if (!selectedEvent) return;
    await fetch(`http://localhost:8080/api/events/${selectedEvent.id}`, {
      method: "DELETE",
    });
    setShowModal(false);
    setSelectedEvent(null);
    await loadEvents();
  };

  // Souhrny týdně (pouze SPORT)
  const getWeeklySummaries = () => {
    const monthStart = startOfMonth(date);
    const monthEnd = endOfMonth(date);
    const weeks = [];

    // Najdeme pondělí prvního týdne zobrazeného měsíce
    let firstWeekStart = startOfWeek(monthStart, { weekStartsOn: 1 });

    // Vygenerujeme max 6 týdnů (měsíční pohled)
    for (let i = 0; i < 6; i++) {
      const weekStart = addDays(firstWeekStart, i * 7);
      const weekEnd = addDays(weekStart, 6);
      // Pokud už je celý týden po měsíci a žádné řádky navíc, skončíme
      if (weekStart > monthEnd && i > 3) break;

      const weekEvents = events.filter(
        (e) => e.category === "SPORT" && isWithinInterval(e.start, { start: weekStart, end: weekEnd })
      );

      const totals = { RUNNING: 0, CYCLING: 0, SWIMMING: 0, OTHER: 0 };
      weekEvents.forEach((ev) => {
        const key = ev.sportType && totals.hasOwnProperty(ev.sportType) ? ev.sportType : "OTHER";
        totals[key] += ev.duration || 0;
      });

      weeks.push({ weekStart, weekEnd, totals });
    }
    return weeks;
  };

  const weeklySummaries = getWeeklySummaries();

  // Auto-resize textarea
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

          {view === "month" && (
            <div className="weekly-summary-grid">
              {weeklySummaries.map((week, i) => (
                <div key={i} className="week-block">
                  <h4>
                    {format(week.weekStart, "d.M.")} – {format(week.weekEnd, "d.M.")}
                  </h4>

                  <div className="sport-item">
                    <img src={runIcon} alt="RUNNING" />
                    <span>
                      {Math.floor(week.totals.RUNNING / 60)}h {week.totals.RUNNING % 60}m
                    </span>
                  </div>
                  <div className="sport-item">
                    <img src={bikeIcon} alt="CYCLING" />
                    <span>
                      {Math.floor(week.totals.CYCLING / 60)}h {week.totals.CYCLING % 60}m
                    </span>
                  </div>
                  <div className="sport-item">
                    <img src={swimIcon} alt="SWIMMING" />
                    <span>
                      {Math.floor(week.totals.SWIMMING / 60)}h {week.totals.SWIMMING % 60}m
                    </span>
                  </div>
                  <div className="sport-item">
                    <img src={otherIcon} alt="OTHER" />
                    <span>
                      {Math.floor(week.totals.OTHER / 60)}h {week.totals.OTHER % 60}m
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

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

                    <label>Typ sportu:</label>
                    <select
                      className="sport-select"
                      value={formData.sportType}
                      onChange={(e) => setFormData({ ...formData, sportType: e.target.value })}
                    >
                      <option value="RUNNING">Běh</option>
                      <option value="CYCLING">Kolo</option>
                      <option value="SWIMMING">Plavání</option>
                      <option value="OTHER">Jiné</option>
                    </select>

                    <label>Popis aktivity:</label>
                    <textarea
                      className="sport-textarea"
                      value={formData.sportDescription}
                      onInput={autoResize}
                      onChange={(e) => setFormData({ ...formData, sportDescription: e.target.value })}
                    />

                    <label>Trvání (minuty):</label>
                    <input
                      className="sport-input"
                      type="number"
                      value={formData.duration}
                      onChange={handleDurationChange}
                    />

                    <label>Vzdálenost (km):</label>
                    <input
                      className="sport-input"
                      type="number"
                      value={formData.distance}
                      onChange={(e) => setFormData({ ...formData, distance: e.target.value })}
                    />

                    <label>Soubor GPX/JSON:</label>
                    <input
                      className="sport-file"
                      type="file"
                      accept=".gpx,.json"
                      onChange={(e) => setFormData({ ...formData, file: e.target.files[0] })}
                    />

                    {formData.filePath && (
                      <div className="file-download">
                        <a
                          href={`http://localhost:8080${formData.filePath}`}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          📄 Stáhnout přiložený soubor
                        </a>
                      </div>
                    )}
                  </div>
                ) : (
                  <>
                    <div className="allday-row">
                      <input
                        type="checkbox"
                        checked={formData.allDay}
                        onChange={(e) => setFormData({ ...formData, allDay: e.target.checked })}
                      />
                      <label>Celý den</label>
                    </div>

                    <label>Popis:</label>
                    <textarea
                      className="desc-textarea"
                      value={formData.description}
                      onInput={autoResize}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    />
                  </>
                )}

                {!formData.allDay && (
                  <div className="time-row">
                    <div>
                      <label>Začátek:</label>
                      <input
                        type="datetime-local"
                        value={formData.start}
                        onChange={handleStartChange}
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
                )}

                <div className="modal-buttons">
                  <button type="submit">{selectedEvent ? "Uložit" : "Přidat"}</button>
                  {selectedEvent && (
                    <button type="button" className="delete-btn" onClick={handleDelete}>
                      Smazat
                    </button>
                  )}
                  <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>
                    Zrušit
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </>
  );
};

export default CalendarPage;
