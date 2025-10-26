import React, { useEffect, useMemo, useState } from "react";
import Header from "../components/Header";
import "../styles/DashboardPage.css";
import {
  parseISO,
  startOfWeek,
  endOfWeek,
  startOfMonth,
  endOfMonth,
  startOfYear,
  endOfYear,
  isWithinInterval,
  format,
  eachDayOfInterval,
  eachWeekOfInterval,
  addDays,
} from "date-fns";
import { cs } from "date-fns/locale";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";

// Mapování SportType z backendu -> uživatelské labely
const SPORT_LABEL = {
  RUNNING: "Běh",
  CYCLING: "Kolo",
  SWIMMING: "Plavání",
  OTHER: "Ostatní",
};

const SPORT_ORDER = ["RUNNING", "CYCLING", "SWIMMING", "OTHER"];

const PERIODS = [
  { key: "day", label: "Den" },
  { key: "week", label: "Týden" },
  { key: "month", label: "Měsíc" },
  { key: "year", label: "Rok" },
  { key: "all", label: "Vše" },
];

// Barvy pouze pro legendu/grafy (konzistentní napříč app)
const SPORT_COLORS = {
  RUNNING: "#ff6a00",
  CYCLING: "#ee0979",
  SWIMMING: "#0088FE",
  OTHER: "#8884d8",
};

// Pomocná: bezpečný parse LocalDateTime (ISO string)
const toDate = (dt) => {
  try {
    return typeof dt === "string" ? parseISO(dt) : new Date(dt);
  } catch {
    return null;
  }
};

const minutesBetween = (start, end) => {
  if (!start || !end) return 0;
  const ms = end.getTime() - start.getTime();
  return Math.max(0, Math.round(ms / 60000));
};

const numberOrZero = (v) => (typeof v === "number" && !isNaN(v) ? v : 0);

const DashboardPage = () => {
  const [loading, setLoading] = useState(true);
  const [events, setEvents] = useState([]);
  const [period, setPeriod] = useState("week");
  const [sportFilter, setSportFilter] = useState("ALL");
  const [error, setError] = useState("");

  useEffect(() => {
    const email = localStorage.getItem("userEmail");
    if (!email) {
      setError("Chybí e-mail přihlášeného uživatele. Nastav jej v profilu.");
      setLoading(false);
      return;
    }

    const load = async () => {
      try {
        setLoading(true);
        const res = await fetch(`/api/events?email=${encodeURIComponent(email)}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        // Normalizace: počítáme jen sportovní eventy (sportType != null)
        const normalized = (data || [])
          .filter((e) => e.sportType) // pouze sport
          .map((e) => {
            const start = toDate(e.startTime);
            const end = toDate(e.endTime);
            const durationMin =
              typeof e.duration === "number" && !isNaN(e.duration)
                ? e.duration
                : minutesBetween(start, end);

            return {
              ...e,
              _start: start,
              _end: end,
              _durationMin: durationMin,
              _distanceKm: numberOrZero(e.distance),
            };
          });

        setEvents(normalized);
        setError("");
      } catch (err) {
        console.error(err);
        setError("Nepodařilo se načíst data pro dashboard.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  // Časový interval podle zvoleného období
  const interval = useMemo(() => {
    const now = new Date();
    switch (period) {
      case "day": {
        const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const end = addDays(start, 1);
        return { start, end };
      }
      case "week":
        return { start: startOfWeek(now, { locale: cs, weekStartsOn: 1 }), end: endOfWeek(now, { locale: cs, weekStartsOn: 1 }) };
      case "month":
        return { start: startOfMonth(now), end: endOfMonth(now) };
      case "year":
        return { start: startOfYear(now), end: endOfYear(now) };
      case "all":
      default:
        return null; // bez omezení
    }
  }, [period]);

  // Filtrování podle sportu a období
  const filtered = useMemo(() => {
    return events.filter((e) => {
      if (sportFilter !== "ALL" && e.sportType !== sportFilter) return false;
      if (!interval) return true;
      if (!e._start) return false;
      return isWithinInterval(e._start, { start: interval.start, end: interval.end });
    });
  }, [events, sportFilter, interval]);

  // Hlavní metriky
  const totals = useMemo(() => {
    const distance = filtered.reduce((s, e) => s + numberOrZero(e._distanceKm), 0);
    const duration = filtered.reduce((s, e) => s + numberOrZero(e._durationMin), 0);
    const activities = filtered.length;
    return {
      distanceKm: distance,
      durationMin: duration,
      activities,
    };
  }, [filtered]);

  // Trend vzdálenosti (x-osa: dny/týdny podle period)
  const trendData = useMemo(() => {
    if (filtered.length === 0) return [];

    // Vytvoříme „bucket“ podle období
    if (period === "day" || period === "week" || period === "month") {
      const start = interval.start;
      const end = interval.end;

      // Den/týden/měsíc -> dny na ose X
      const days = eachDayOfInterval({ start, end });
      const map = new Map(days.map((d) => [format(d, "yyyy-MM-dd"), 0]));

      filtered.forEach((e) => {
        if (!e._start) return;
        const key = format(e._start, "yyyy-MM-dd");
        if (map.has(key)) {
          map.set(key, map.get(key) + numberOrZero(e._distanceKm));
        }
      });

      return Array.from(map.entries()).map(([day, val]) => ({
        name: format(parseISO(day), "d.M.", { locale: cs }),
        distance: Number(val.toFixed(2)),
      }));
    }

    if (period === "year") {
      // Rok -> týdny
      const start = interval.start;
      const end = interval.end;
      const weeks = eachWeekOfInterval({ start, end }, { weekStartsOn: 1 });
      const map = new Map(weeks.map((w) => [format(w, "yyyy-'W'II"), 0]));

      filtered.forEach((e) => {
        if (!e._start) return;
        const wkKey = format(startOfWeek(e._start, { weekStartsOn: 1 }), "yyyy-'W'II");
        if (map.has(wkKey)) {
          map.set(wkKey, map.get(wkKey) + numberOrZero(e._distanceKm));
        }
      });

      return Array.from(map.entries()).map(([wk, val]) => ({
        name: wk.replace("W"
