import React, { useState, useEffect, useRef } from "react";
import { useParams } from "react-router-dom";
import "../../App.css"; // Import styles
import "./Incident.css"; 
import { fetchWithAuth } from "../../utils/api";

const API_URL = process.env.REACT_APP_API_URL;

const IncidentDetails = () => {
  const { id } = useParams();
  const [incident, setIncident] = useState(null);
  const [loading, setLoading] = useState(true);
  const [mediaFiles, setMediaFiles] = useState([]);
  const [selectedMedia, setSelectedMedia] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const modalRef = useRef(null);

  useEffect(() => {
    fetchIncidentDetails();
    fetchMediaFiles();
  }, [id]);

  const fetchIncidentDetails = async () => {
    try {
      const response = await fetchWithAuth(`${API_URL}/user/incidents/${id}`);
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
      const response = await fetchWithAuth(`${API_URL}/user/media/incident/${id}`);
      if (!response.ok) throw new Error("Failed to fetch media");

      const mediaUrls = await response.json(); // ✅ List of media URLs
      
      // Получаем токен авторизации
      const token = localStorage.getItem('token');
      
      // Добавляем токен к URL или используем blob подход
      const files = await Promise.all(mediaUrls.map(async (url) => {
        // Определяем тип файла
        const fileType = url.endsWith(".mp4") || url.endsWith(".mov") ? "video" : "image";
        
        // Делаем запрос с авторизацией для каждого медиафайла
        const mediaResponse = await fetch(url, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!mediaResponse.ok) {
          console.error(`Failed to fetch media file: ${url}`);
          return null;
        }
        
        // Создаем blob и URL для него
        const blob = await mediaResponse.blob();
        const fileUrl = URL.createObjectURL(blob);
        
        return {
          url: fileUrl,
          type: fileType
        };
      }));
      
      // Фильтруем null значения (если были ошибки при загрузке)
      setMediaFiles(files.filter(file => file !== null));
    } catch (error) {
      console.error("Error fetching media:", error);
    }
  };

  if (loading) return <p>Загрузка...</p>;
  if (!incident) return <p>Инцидент не найден</p>;
  
  const openModal = (file) => {
    setSelectedMedia(file);
    setShowModal(true);
    document.body.style.overflow = 'hidden'; // Предотвращаем прокрутку страницы
  };

  const closeModal = () => {
    setShowModal(false);
    setSelectedMedia(null);
    document.body.style.overflow = 'auto'; // Возвращаем прокрутку страницы
  };
  
  // Закрытие модального окна при клике вне изображения
  const handleModalClick = (e) => {
    if (modalRef.current && !modalRef.current.contains(e.target)) {
      closeModal();
    }
  };

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
              <video 
                key={index} 
                controls 
                className="media-item"
                onClick={() => openModal(file)}
              >
                <source src={file.url} type="video/mp4" />
                Ваш браузер не поддерживает видео.
              </video>
            ) : (
              <img 
                key={index} 
                src={file.url} 
                alt={`Incident ${index}`} 
                className="media-item" 
                onClick={() => openModal(file)}
              />
            )
          )}
        </div>
      )}
      {mediaFiles.length === 0 && <p>Нет медиафайлов</p>}
      
      {/* Модальное окно для просмотра медиа */}
      {showModal && (
        <div className="media-modal-overlay" onClick={handleModalClick}>
          <div className="media-modal-content" ref={modalRef}>
            <button className="modal-close-btn" onClick={closeModal}>×</button>
            {selectedMedia && selectedMedia.type === "video" ? (
              <video controls className="modal-media">
                <source src={selectedMedia.url} type="video/mp4" />
                Ваш браузер не поддерживает видео.
              </video>
            ) : (
              <img 
                src={selectedMedia?.url} 
                alt="Увеличенное изображение" 
                className="modal-media" 
              />
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default IncidentDetails;
