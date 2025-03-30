import React, { useState, useEffect } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { format, isSameDay, parseISO } from "date-fns";
import { fetchWithAuth } from "../../utils/api";

const API_URL = process.env.REACT_APP_API_URL;

const Graph = ({ startDate, endDate }) => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const isSingleDay = isSameDay(startDate, endDate);

  useEffect(() => {
    if (!startDate || !endDate) return;
    fetchGraphData(startDate, endDate);
  }, [startDate, endDate]);

  const fetchGraphData = async (start, end) => {
    setLoading(true);
    const formattedStart = format(start, "dd-MM-yyyy");
    const formattedEnd = format(end, "dd-MM-yyyy");

    try {
      const response = await fetchWithAuth(
        `${API_URL}/user/incidents/search/date-range?startDate=${formattedStart}&endDate=${formattedEnd}`
      );
      if (!response.ok) throw new Error("Failed to fetch data");
      const incidents = await response.json();

      const formattedData = isSingleDay
        ? processIncidentDataByHour(incidents)
        : processIncidentDataByDay(incidents);

      setData(formattedData);
    } catch (error) {
      console.error("Error fetching graph data:", error);
      setData([]); // Ensure no data is set on failure
    } finally {
      setLoading(false);
    }
  };

  const processIncidentDataByHour = (incidents) => {
    if (!incidents || incidents.length === 0) return [];

    const hourlyCount = {};
    incidents.forEach((incident) => {
      const date = new Date(incident.createdAt);
      const hour = date.getHours();
      const formattedTime = `${hour}:00`;
      hourlyCount[formattedTime] = (hourlyCount[formattedTime] || 0) + 1;
    });

    return Object.keys(hourlyCount)
      .sort((a, b) => parseInt(a) - parseInt(b))
      .map((time) => ({ time, count: hourlyCount[time] }));
  };

  const processIncidentDataByDay = (incidents) => {
    if (!incidents || incidents.length === 0) return [];

    const dailyCount = {};
    incidents.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

    incidents.forEach((incident) => {
      const date = format(parseISO(incident.createdAt), "dd-MM-yyyy");
      dailyCount[date] = (dailyCount[date] || 0) + 1;
    });

    return Object.keys(dailyCount)
      .sort((a, b) => new Date(a) - new Date(b))
      .map((date) => ({ time: date, count: dailyCount[date] }));
  };

  return (
    <div style={{ width: "100%", height: "200px", display: "flex", justifyContent: "center", alignItems: "center" }}>
      {loading ? (
        <p>Загрузка...</p>
      ) : data.length === 0 ? (
        <p style={{ fontSize: "16px", color: "gray" }}>Нет данных за выбранный период</p>
      ) : (
        <ResponsiveContainer width="100%" height={200}>
          <LineChart data={data}>
            <XAxis dataKey="time" />
            <YAxis />
            <Tooltip />
            <Line type="monotone" dataKey="count" stroke="#82ca9d" strokeWidth={2} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  );
};

export default Graph;
