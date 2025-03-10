import React from "react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";

const data = [
  { time: "8:00", count: 30 },
  { time: "10:00", count: 50 },
  { time: "12:00", count: 20 },
  { time: "14:00", count: 80 },
  { time: "16:00", count: 150 },
  { time: "18:00", count: 250 },
  { time: "20:00", count: 180 },
  { time: "22:00", count: 220 },
];

const Graph = () => {
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
