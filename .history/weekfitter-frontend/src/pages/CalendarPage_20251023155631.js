import React, { useState, useEffect } from "react";
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import { format, parse, startOfWeek, getDay } from "date-fns";
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
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    start: "",
    end: "",
    category: "SPORT",
  });

  // 🔹 Načtení událostí z backendu
  useEffect(() => {
    fetch("http://localhost:8080/api/events")
      .then((res) => res.json())
      .then((data) => {
        const formatted = data.map((event) => ({
          id: event.id,
          title: event.title,
          start: new Date(event.startTime),
          end: new Date(event.endTime),
          description: event.description,
          category: event.category,
        }));
        setEvents(formatted);
      })
      .catch((err) => console.error("Chyba při načítání událostí:", err));
  }, []);

  // 🔹 Stylování událostí podle typu
  const getEventStyle = (event) => {
    let bgColor = "#ff6a00";
    switch (event.category) {
      case "SPORT":
        bgColor = "#28a745";
        break;
      case "WORK":
        bgColor = "#007bff";
        break;
      case "SCHOOL":
        bgColor = "#ffc107";
        break;
      case "REST":
        bgColor = "#6f42c1";
        break;
      default:
        bgColor = "#ff6a00";
    }
    return {
      style: {
        backgroundColor: bgColor,
        borderRadius: "8px",
        color: "white",
        border: "none",
      },
    };
  };

  // 🔸 Kliknutí na den/časový úsek → otevře formulář
  c
