import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import "../../App.css"; // Import styles
import "./Incident.css"; 

const API_URL = process.env.REACT_APP_API_URL;

const IncidentDetails = () => {
  const { id } = useParams();
  const [incident, setIncident] = useState(null);
  const [loading, setLoading] = useState(true);
  const [mediaFiles, setMediaFiles] = useState([]);

  useEffect(() => {
    fetchIncidentDetails();
    fetchMediaFiles();
  }, [id]);

  const fetchIncidentDetails = async () => {
    try {
      const response = await fetch(`${API_URL}/incidents/${id}`);
      if (!response.ok) throw new Error("Failed to fetch incident details");
      const data = await response.json();
      setIncident(data);
    } catch (error) {
      console.error("Error fetching incident details:", error);
      setIncident(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchMediaFiles = async () => {
    try {
      const response = await fetch(`${API_URL}/media/incident/${id}`);
      if (!response.ok) throw new Error("Failed to fetch media");

      const mediaUrls = await response.json(); // ✅ List of media URLs
      const files = mediaUrls.map((url) => ({
        url,
        type: url.endsWith(".mp4") || url.endsWith(".mov") ? "video" : "image",
      }));

      setMediaFiles(files);
    } catch (error) {
      console.error("Error fetching media:", error);
    }
  };

  if (loading) return <p>Загрузка...</p>;
  if (!incident) return <p>Инцидент не найден</p>;

  return (
    <div className="incident-details-container">
      <h2>{incident.title}</h2>
      <p><strong>Пользователь:</strong> {incident.username}</p>
      <p><strong>Адрес:</strong> {incident.locationResponseDto?.address}</p>
      <p><strong>Тип:</strong> {incident.incidentTypeResponseDto?.name}</p>
      <p><strong>Описание:</strong> {incident.description}</p>
      <p><strong>Дата:</strong> {new Date(incident.createdAt).toLocaleString()}</p>

      {/* 🔹 Media Album (Horizontal Scroll) */}
      {mediaFiles.length > 0 && (
        <div className="scrollable-album">
          {mediaFiles.map((file, index) =>
            file.type === "video" ? (
              <video key={index} controls className="media-item">
                <source src={file.url} type="video/mp4" />
                Ваш браузер не поддерживает видео.
              </video>
            ) : (
              <img key={index} src={file.url} alt={`Incident ${index}`} className="media-item" />
            )
          )}
        </div>
      )}
    </div>
  );
};

export default IncidentDetails;
