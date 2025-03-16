import React, { useState, useEffect } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { format } from "date-fns";

const API_URL = process.env.REACT_APP_API_URL;

const Graph = ({ selectedDate }) => {
  const [data, setData] = useState([]);

  useEffect(() => {
    if (!selectedDate) return;
    fetchGraphData(selectedDate);
  }, [selectedDate]);

  const fetchGraphData = async (date) => {
    const formattedDate = format(date, "dd-MM-yyyy"); // Convert date to DD-MM-YYYY
    try {
      const response = await fetch(`${API_URL}/incidents/search/date?date=${formattedDate}`);
      if (!response.ok) throw new Error("Failed to fetch data");
      const incidents = await response.json();

      const formattedData = processIncidentData(incidents);
      setData(formattedData);
    } catch (error) {
      console.error("Error fetching graph data:", error);
    }
  };

  const processIncidentData = (incidents) => {
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

  return (
    <ResponsiveContainer width="100%" height={200}>
      <LineChart data={data}>
        <XAxis dataKey="time" />
        <YAxis />
        <Tooltip />
        <Line type="monotone" dataKey="count" stroke="#82ca9d" strokeWidth={2} />
      </LineChart>
    </ResponsiveContainer>
  );
};

export default Graph;
