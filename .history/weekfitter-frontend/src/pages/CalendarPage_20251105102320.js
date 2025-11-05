


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

    // === Export měsíčního pohledu jako PNG ===
  const handleExportPNG = async () => {
    if (view !== Views.MONTH) {
      alert("Export je dostupný pouze v měsíčním pohledu.");
      return;
    }

    const exportElement = document.querySelector(".calendar-with-summary");
    if (!exportElement) {
      alert("Nelze najít obsah k exportu.");
      return;
    }

    try {
      const canvas = await html2canvas(exportElement, {
        backgroundColor: "#ffffff",
        scale: 2,
        useCORS: true,
      });

      const link = document.createElement("a");
      link.download = `WeekFitter-Mesic-${format(date, "MM-yyyy")}.png`;
      link.href = canvas.toDataURL("image/png");
      link.click();
    } catch (err) {
      console.error("Chyba při exportu:", err);
      alert("Došlo k chybě při exportu kalendáře.");
    }
  };


  return (
    <>
      <Header />
      <main className="calendar-container">
        <div className="calendar-card">
          <h2>Kalendář aktivit</h2>

          {view === Views.MONTH && (
            <button
              className="export-btn"
              onClick={handleExportPNG}
              title="Uložit aktuální měsíc jako obrázek"
            >
              Exportovat jako PNG
            </button>
          )}


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
                            href={`${API_URL}${formData.filePath}`}
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
                        className="add-notify-btn"
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
                          className="close-notify-btn"
                          onClick={() => setNotifications(notifications.filter((_, idx) => idx !== i))}
                        >
                          X
                        </button>
                      </div>
                    ))}

                    {notifications.length > 0 && (
                      <button
                        type="button"
                        onClick={() => setNotifications([...notifications, 60])}
                        className="add-notify-btn"
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
