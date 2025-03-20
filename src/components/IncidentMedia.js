import React, { useState, useEffect } from "react";

const API_URL = process.env.REACT_APP_API_URL;

const IncidentMedia = ({ incidentId }) => {
  const [mediaFiles, setMediaFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMedia();
  }, [incidentId]);

  const fetchMedia = async () => {
    try {
      const response = await fetch(`${API_URL}/media/incident/${incidentId}`);
      if (!response.ok) throw new Error("Failed to fetch media");

      // Convert response to a blob
      const blob = await response.blob();

      // Create a URL for the blob
      const contentType = response.headers.get("Content-Type");
      const fileUrl = URL.createObjectURL(blob);

      setMediaFiles([{ url: fileUrl, type: contentType }]);
    } catch (error) {
      console.error("Error fetching media:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {loading ? (
        <p>Загрузка...</p>
      ) : mediaFiles.length === 0 ? (
        <p>Нет медиафайлов</p>
      ) : (
        <div className="media-container">
          {mediaFiles.map((file, index) =>
            file.type.startsWith("image/") ? (
              <img key={index} src={file.url} alt={`media-${index}`} className="media-image" />
            ) : file.type.startsWith("video/") ? (
              <video key={index} controls className="media-video">
                <source src={file.url} type={file.type} />
                Ваш браузер не поддерживает видео.
              </video>
            ) : null
          )}
        </div>
      )}
    </div>
  );
};

export default IncidentMedia;
