import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import {
  format,
  parse,
  startOfWeek,
  getDay,
  addMinutes,
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

  // === Kliknutí do buňky kalendáře ===
  const handleSelectSlot = (slotInfo) => {
    const start = slotInfo.start;
    const end = addMinutes(start, 30);

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

  // === Kliknutí na událost (otevře detail / úpravu) ===
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
      allDay: category === "
