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

  const [notifications, setNotifications] = useState([60]);
  const [view, setView] = useState(Views.MONTH);
  const [date, setDate] = useState(new Date());

  // === Načtení událostí pouze pro přihlášeného uživatele ===
  const loadEvents = async () => {
    const email = localStorage.getItem("userEmail");
    if (!email) {
      console.error("Uživatel není přihlášen – chybí e-mail v localStorage.");
      setEvents([]);
      return;
    }

    try {
      const res = await fetch(`http://localhost:8080/api/events?email=${encodeURIComponent(email)}`);
      const data = await res.json();

      if (!Array.isArray(data)) {
        console.error("Server nevrátil pole událostí:", data);
        setEvents([]);
        return;
      }

      // sjednocení category/activityType + bezpečné parsování času
      const formatted = data.map((event) => {
        const category = event.category ?? event.category ?? "OTHER";
        const start = new Date(event.startTime);
        const end = new Date(event.endTime);
        return {
          id: event.id,
          title: event.title,
          start,
          end,
          description: event.description,
          category,
          allDay: Boolean(event.allDay),
          duration: event.duration,
          distance: event.distance,
          sportDescription: event.sportDescription,
          sportType: event.sportType ?? "OTHER",
          filePath: event.filePath,
        };
      });

      setEvents(
        formatted.filter(
          (e) =>
            e.start instanceof Date &&
            !isNaN(e.start) &&
            e.end instanceof Date &&
            !isNaN(e.end)
        )
      );
    } catch (error) {
      console.error("Chyba při načítání událostí:", error);
      setEvents([]);
    }
  };

  useEffect(() => {
    loadEvents();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
          <img src={iconSrc} alt={event.sportType || "SPORT"} className="event-icon-img" />
          <span className="event-title">{event.title}</span>
        </div>
      );
    }
    return <div className="event-title">{event.title}</div>;
  };

  // === Dynamické zvětšování textarea ===
  const autoResize = (e) => {
    e.target.style.height = "auto";
    e.target.style.height = `${e.target.scrollHeight}px`;
  };

  // === Kliknutí do buňky kalendáře ===
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

  // === Kliknutí na událost ===
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

  // === Změna kategorie (SPORT přepíná allDay na false) ===
  const handleCategoryChange = (e) => {
    const category = e.target.value;
    setFormData((prev) => ({
      ...prev,
      category,
      allDay: category === "SPORT" ? false : prev.allDay,
    }));
  };

  // === Změna trvání → přepočítá konec ===
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

  // === Změna začátku → přepočítá konec podle duration nebo +1h ===
  const handleStartChange = (e) => {
    const newStart = new Date(e.target.value);

    // Pokud je zadáno trvání, konec = start + duration
    if (formData.duration && !isNaN(parseInt(formData.duration, 10))) {
      const minutes = parseInt(formData.duration, 10);
      const newEnd = addMinutes(newStart, minutes);
      setFormData((prev) => ({
        ...prev,
        start: format(newStart, "yyyy-MM-dd'T'HH:mm"),
        end: format(newEnd, "yyyy-MM-dd'T'HH:mm"),
      }));
    } else {
      // Pokud není trvání a konec není ručně měněn → posuň konec o hodinu
      const prevEnd = new Date(formData.end);
      const prevStart = new Date(formData.start);

      const userManuallyChangedEnd =
        Math.abs(prevEnd - prevStart - 30 * 60 * 1000) > 60 * 1000; // odchylka od původního 30 min defaultu

      // nastav konec +1h pouze, pokud ho uživatel dosud neměnil ručně
      const newEnd = userManuallyChangedEnd
        ? prevEnd
        : new Date(newStart.getTime() + 60 * 60 * 1000);

      setFormData((prev) => ({
        ...prev,
        start: format(newStart, "yyyy-MM-dd'T'HH:mm"),
        end: format(newEnd, "yyyy-MM-dd'T'HH:mm"),
      }));
    }
  };


  // Pomocná funkce: sjednocené payloady pro backend (posíláme i activityType)
  const buildPayloadFromEvent = (base, overrides = {}) => {
    const resolvedCategory = (overrides.category ?? base.category) || "OTHER";
    const isSport = resolvedCategory === "SPORT";

    const startVal = overrides.start
      ? new Date(overrides.start)
      : new Date(base.start);
    const endVal = overrides.end
      ? new Date(overrides.end)
      : new Date(base.end);

    const startTime = format(startVal, "yyyy-MM-dd'T'HH:mm");
    const endTime = format(endVal, "yyyy-MM-dd'T'HH:mm");

    return {
      id: base.id,
      title: overrides.title ?? base.title,
      description: isSport
        ? (overrides.sportDescription ?? base.sportDescription ?? "")
        : (overrides.description ?? base.description ?? ""),
      startTime,
      endTime,
      category: resolvedCategory,     // nevadí posílat oba
      allDay: isSport ? false : Boolean(overrides.allDay ?? base.allDay),
      duration: isSport ? (Number(overrides.duration ?? base.duration) || null) : null,
      distance: isSport ? (Number(overrides.distance ?? base.distance) || null) : null,
      sportDescription: isSport ? (overrides.sportDescription ?? base.sportDescription ?? "") : null,
      sportType: isSport ? (overrides.sportType ?? base.sportType ?? "OTHER") : null,
      filePath: overrides.filePath ?? base.filePath ?? null,
    };
  };

  // === Uložení (vytvoření/aktualizace) události ===
  const handleSubmit = async (e) => {
    e.preventDefault();
    const email = localStorage.getItem("userEmail");
    if (!email) {
      alert("Uživatel není přihlášen.");
      return;
    }

    // Upload souboru (pokud je)
    let uploadedFilePath = formData.filePath;
    if (formData.file) {
      const uploadData = new FormData();
      uploadData.append("file", formData.file);

      try {
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
      } catch (error) {
        alert("Chyba spojení s backendem při nahrávání souboru.");
        return;
      }
    }

    // Payload pro backend – posíláme i activityType
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
      filePath: uploadedFilePath || null,
      notifications,
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
        const msg = await res.text();
        alert("Chyba při ukládání události: " + msg);
        return;
      }

      setShowModal(false);
      setSelectedEvent(null);
      await loadEvents();
    } catch (error) {
      alert("Chyba spojení s backendem při ukládání události.");
    }
  };

  // === Smazání události ===
  const handleDelete = async () => {
    if (!selectedEvent) return;
    try {
      await fetch(`http://localhost:8080/api/events/${selectedEvent.id}`, {
        method: "DELETE",
      });
      setShowModal(false);
      setSelectedEvent(null);
      await loadEvents();
    } catch (error) {
      alert("Chyba při mazání události.");
    }
  };

  // === Drag & Drop (přesun) — pošli activityType + email + optimistický update ===
  const handleEventDrop = async ({ event, start, end }) => {
    const email = localStorage.getItem("userEmail");
    const payload = buildPayloadFromEvent(
      { ...event, start: event.start, end: event.end },
      { start, end }
    );

    // Optimistická aktualizace UI
    setEvents((prev) =>
      prev.map((e) =>
        e.id === event.id ? { ...e, start: new Date(start), end: new Date(end) } : e
      )
    );

    try {
      const res = await fetch(
        `http://localhost:8080/api/events/${event.id}?email=${encodeURIComponent(email)}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        }
      );
      if (!res.ok) {
        console.error("Chyba při přesunu události:", await res.text());
        await loadEvents();
      } else {
        await loadEvents();
      }
    } catch (error) {
      console.error("Chyba při přesunu události:", error);
      await loadEvents();
    }
  };

  // === Resize (změna délky) — stejný přístup jako u drop ===
  const handleEventResize = async ({ event, start, end }) => {
    const email = localStorage.getItem("userEmail");
    const payload = buildPayloadFromEvent(
      { ...event, start: event.start, end: event.end },
      { start, end }
    );

    setEvents((prev) =>
      prev.map((e) =>
        e.id === event.id ? { ...e, start: new Date(start), end: new Date(end) } : e
      )
    );

    try {
      const res = await fetch(
        `http://localhost:8080/api/events/${event.id}?email=${encodeURIComponent(email)}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        }
      );
      if (!res.ok) {
        console.error("Chyba při změně délky události:", await res.text());
        await loadEvents();
      } else {
        await loadEvents();
      }
    } catch (error) {
      console.error("Chyba při změně délky události:", error);
      await loadEvents();
    }
  };

  // === Souhrn sportů v měsíčním pohledu (všechny týdny měsíce) ===
  const renderWeeklySummaryAllWeeks = () => {
    if (view !== Views.MONTH) return null;

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
      <div className="calendar-with-summary">
        <div className="calendar-left">
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
        <div className="calendar-summary-column">
          {weeks.map((weekStart, idx) => {
            const weekEnd = endOfWeek(weekStart, { weekStartsOn: 1 });
            const weekEvents = events.filter(
              (e) =>
                e.category === "SPORT" &&
                e.start >= weekStart &&
                e.start <= weekEnd
            );
            const totals = { RUNNING: 0, CYCLING: 0, SWIMMING: 0, OTHER: 0 };
            weekEvents.forEach((e) => {
              const dur = e.duration || 0;
              const key = e.sportType && totals[e.sportType] !== undefined ? e.sportType : "OTHER";
              totals[key] += dur;
            });
            return (
              <div key={idx} className="summary-row">
                <div className="summary-week-label">
                  {format(weekStart, "d.M.")} – {format(weekEnd, "d.M.")}
                </div>
                <div className="summary-icons">
                  <div className="sport-item">
                    <img src={runIcon} alt="běh" />
                    <span>{toHours(totals.RUNNING)}</span>
                  </div>
                  <div className="sport-item">
                    <img src={bikeIcon} alt="kolo" />
                    <span>{toHours(totals.CYCLING)}</span>
                  </div>
                  <div className="sport-item">
                    <img src={swimIcon} alt="plavání" />
                    <span>{toHours(totals.SWIMMING)}</span>
                  </div>
                  <div className="sport-item">
                    <img src={otherIcon} alt="jiné" />
                    <span>{toHours(totals.OTHER)}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <>
      <Header />
      <main className="calendar-container">
        <div className="calendar-card">
          <h2>Kalendář aktivit</h2>

          {view === Views.MONTH ? (
            renderWeeklySummaryAllWeeks()
          ) : (
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
          )}

          {/* Modal */}
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
                        onChange={(e) => setFormData({ ...formData, file: e.target.files?.[0] || null })}
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
                          id="allday"
                          type="checkbox"
                          checked={formData.allDay}
                          onChange={(e) => setFormData({ ...formData, allDay: e.target.checked })}
                        />
                        <label htmlFor="allday">Celý den</label>
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

                  <div className="notification-section">
                    <h4>Upozornění</h4>

                    {notifications.length === 0 && (
                      <button
                        type="button"
                        onClick={() => setNotifications([60])}
                        className="btn-add"
                      >
                        Přidat upozornění
                      </button>
                    )}

                    {notifications.map((min, i) => (
                      <div key={i} className="notify-row">
                        <label>Upozornit před začátkem:</label>
                        <select
                          className="notify-select"
                          value={min}
                          onChange={(e) => {
                            const v = Number(e.target.value);
                            const copy = [...notifications];
                            copy[i] = v;
                            setNotifications(copy);
                          }}
                        >
                          <option value={5}>5 minut</option>
                          <option value={15}>15 minut</option>
                          <option value={30}>30 minut</option>
                          <option value={60}>1 hodina</option>
                          <option value={120}>2 hodiny</option>
                          <option value={1440}>1 den</option>
                          <option value={2880}>2 dny</option>
                          <option value={10080}>1 týden</option>
                        </select>

                        <button
                          type="button"
                          className="btn-delete"
                          onClick={() => setNotifications(notifications.filter((_, idx) => idx !== i))}
                        >
                        </button>
                      </div>
                    ))}

                    {notifications.length > 0 && (
                      <button
                        type="button"
                        onClick={() => setNotifications([...notifications, 60])}
                        className="btn-add"
                      >
                        Další upozornění
                      </button>
                    )}
                  </div>


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
        </div>
      </main>
    </>
  );
};

export default CalendarPage;
