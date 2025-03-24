import React, { useState, useEffect } from "react";
import DashboardSection from "../dashboard-section/DashboardSection";
import "react-datepicker/dist/react-datepicker.css";
import DatePicker from "react-datepicker";
import "../../App.css";
import { format, isSameDay, parseISO, parse } from "date-fns";
import { useNavigate, useSearchParams } from "react-router-dom";

const API_URL = process.env.REACT_APP_API_URL;

const History = () => {
  const navigate = useNavigate(); // For navigating to the incident details
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(false);

  // Filters
  const [searchTitle, setSearchTitle] = useState("");
  const [searchDescription, setSearchDescription] = useState("");
  const [selectedType, setSelectedType] = useState("");
  const [typedAddress, setTypedAddress] = useState("");
  const [selectedLocationId, setSelectedLocationId] = useState("");

  // User Filter
  const [selectedUserId, setSelectedUserId] = useState("");

  // Date Range
  const [dateRange, setDateRange] = useState([null, null]); // [startDate, endDate]
  const [startDateStr, endDateStr] = dateRange;

  // Fetched Data (Incident Types, Locations, Users)
  const [incidentTypes, setIncidentTypes] = useState([]);
  const [locations, setLocations] = useState([]);
  const [users, setUsers] = useState([]);

  const [shouldTriggerSearch, setShouldTriggerSearch] = useState(false);
  const [resetTriggered, setResetTriggered] = useState(false);

  // On mount, extract dates from URL (if present)
  useEffect(() => {
    fetchIncidentTypes();
    fetchLocations();
    fetchUsers();

    const storedRange = JSON.parse(
      window.localStorage.getItem("historyDateRange")
    );
    if (storedRange && storedRange.length === 2) {
      const [start, end] = storedRange;
      const parsedStart = parse(start, "dd-MM-yyyy", new Date());
      const parsedEnd = parse(end, "dd-MM-yyyy", new Date());

      setDateRange([parsedStart, parsedEnd]);
      setShouldTriggerSearch(true); // Trigger fetchIncidents in another useEffect
      window.localStorage.removeItem("historyDateRange");
    } else {
      fetchIncidents(); // normal load with no filter
    }
  }, []);

  useEffect(() => {
    if (shouldTriggerSearch && startDateStr && endDateStr) {
      fetchIncidents();
      setShouldTriggerSearch(false);
    }
  }, [shouldTriggerSearch, startDateStr, endDateStr]);

  useEffect(() => {
    if (resetTriggered) {
      fetchIncidents(); // ✅ Now this sees truly reset filters
      setResetTriggered(false); // Reset the flag
    }
  }, [resetTriggered]);

  const fetchIncidentTypes = async () => {
    try {
      const response = await fetch(`${API_URL}/incident-types`);
      if (!response.ok) throw new Error("Failed to fetch incident types");
      const data = await response.json();
      setIncidentTypes(data);
    } catch (error) {
      console.error("Error fetching incident types:", error);
    }
  };

  const fetchLocations = async () => {
    try {
      const response = await fetch(`${API_URL}/locations`);
      if (!response.ok) throw new Error("Failed to fetch locations");
      const data = await response.json();
      setLocations(data);
    } catch (error) {
      console.error("Error fetching locations:", error);
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await fetch(`${API_URL}/users`);
      if (!response.ok) throw new Error("Failed to fetch users");
      const data = await response.json();
      setUsers(data);
    } catch (error) {
      console.error("Error fetching users:", error);
    }
  };

  const fetchIncidents = async () => {
    setLoading(true);
    try {
      let url = `${API_URL}/incidents/search/filters?`;

      // Title, Description
      if (searchTitle) url += `&title=${encodeURIComponent(searchTitle)}`;
      if (searchDescription)
        url += `&description=${encodeURIComponent(searchDescription)}`;

      // Incident Type
      if (selectedType) url += `&incidentTypeId=${selectedType}`;

      // Location
      if (selectedLocationId) {
        url += `&locationId=${selectedLocationId}`;
      } else if (typedAddress) {
        url += `&address=${encodeURIComponent(typedAddress)}`;
      }

      // User
      if (selectedUserId) {
        url += `&userId=${selectedUserId}`;
      }

      // Date Range
      if (startDateStr && endDateStr) {
        const formattedStart = format(startDateStr, "dd-MM-yyyy");
        const formattedEnd = format(endDateStr, "dd-MM-yyyy");
        url += `&startDateStr=${formattedStart}&endDateStr=${formattedEnd}`;
      }

      const response = await fetch(url);
      if (!response.ok) throw new Error("Failed to fetch incidents");

      const data = await response.json();
      setIncidents(data);
    } catch (error) {
      console.error("Error fetching incidents:", error);
      setIncidents([]);
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setSearchTitle("");
    setSearchDescription("");
    setSelectedType("");
    setTypedAddress("");
    setSelectedLocationId("");
    setSelectedUserId("");
    setDateRange([null, null]);
    setResetTriggered(true); // ✅ Triggers the effect below
  };

  return (
    <div className="history-content">
      <h1 style={{ marginBottom: "0.5rem" }}>История обращений</h1>

      {/* Filters Section (One Line) */}
      <div className="filters-container">
        {/* Row 1: Название и Описание */}
        <input
          type="text"
          placeholder="Название..."
          value={searchTitle}
          onChange={(e) => setSearchTitle(e.target.value)}
        />
        <input
          type="text"
          placeholder="Описание..."
          value={searchDescription}
          onChange={(e) => setSearchDescription(e.target.value)}
        />

        {/* Row 2: Селекты */}
        <select
          value={selectedType}
          onChange={(e) => setSelectedType(e.target.value)}
        >
          <option value="">Все типы</option>
          {incidentTypes.map((type) => (
            <option key={type.id} value={type.id}>
              {type.name}
            </option>
          ))}
        </select>

        <select
          value={selectedLocationId}
          onChange={(e) => {
            setSelectedLocationId(e.target.value);
            setTypedAddress("");
          }}
        >
          <option value="">Выбрать место</option>
          {locations.map((location) => (
            <option key={location.id} value={location.id}>
              {location.address}
            </option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Адрес вручную..."
          value={typedAddress}
          onChange={(e) => {
            setTypedAddress(e.target.value);
            setSelectedLocationId("");
          }}
        />

        <select
          value={selectedUserId}
          onChange={(e) => setSelectedUserId(e.target.value)}
        >
          <option value="">Все пользователи</option>
          {users.map((user) => (
            <option key={user.id} value={user.id}>
              {user.username || user.name || `User #${user.id}`}
            </option>
          ))}
        </select>

        {/* Date Picker */}
        <DatePicker
          selectsRange={true}
          startDate={startDateStr}
          endDate={endDateStr}
          onChange={(update) => setDateRange(update)}
          dateFormat="dd-MM-yyyy"
          placeholderText="Выберите даты"
          className="date-picker"
        />

        {/* Buttons */}
        <button className="search-button" onClick={fetchIncidents}>
          🔍 Поиск
        </button>
        <button className="clear-button" onClick={clearFilters}>
          ❌ Очистить
        </button>
      </div>

      {/* Incidents List */}
      <DashboardSection title="Все обращения">
        <div className="incidents-container full-history">
          {loading ? (
            <p>Загрузка...</p>
          ) : incidents.length === 0 ? (
            <p style={{ color: "gray" }}>Нет результатов</p>
          ) : (
            incidents.map((incident, index) => (
              <div key={index} className="incident-card">
                <strong>{incident.title}</strong>
                <p>
                  <strong>Пользователь:</strong> {incident.username}
                </p>
                <p>
                  <strong>Адрес:</strong>{" "}
                  {incident.locationResponseDto?.address || "Нет адреса"}
                </p>
                <p>
                  <strong>Тип:</strong>{" "}
                  {incident.incidentTypeResponseDto?.name || "Не указан"}
                </p>
                <p>
                  <strong>Описание:</strong> {incident.description}
                </p>
                <p>
                  <strong>Дата:</strong>{" "}
                  {format(parseISO(incident.createdAt), "dd-MM-yyyy HH:mm")}
                </p>
                <a
                  href={`/incidents/${incident.id}`}
                  className="read-more"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Читать далее...
                </a>
              </div>
            ))
          )}
        </div>
      </DashboardSection>
    </div>
  );
};

export default History;
