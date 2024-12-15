import json
import os
from dotenv import load_dotenv
import requests
from utils.logger import logger


load_dotenv()
ip = os.getenv("HOST_IP")

async def send_incident_to_backend(zip_buffer, incident_data, update):
    """
    Sends incident data to the backend in the format of incidentRequestDto.
    """
    url = f"http://{ip}/incidents"

    # Подготовка данных для JSON-запроса
    username = update.effective_user.username
    incident_type_id = 1  # Укажите подходящий ID типа инцидента

    incident_request_dto = {
        "title": incident_data["title"],
        "description": incident_data["description"],
        "username": username,
        "incidentTypeId": incident_type_id,
        "locationRequestDto": {
            "address": incident_data["address"],
            "latitude": incident_data["location"]["latitude"],
            "longitude": incident_data["location"]["longitude"]
        }
    }
    
    # Сериализация JSON-данных в строку
    incident_request_json = json.dumps(incident_request_dto)

    # Подготовка файлов для multipart/form-data
    files = {
        # JSON передаётся как файл
        "incidentRequestDto": ("incident_request.json", incident_request_json, "application/json"),
        # ZIP-архив с файлами
        "incidentFiles": ("incident_media.zip", zip_buffer, "application/zip")
    }

    try:
        response = requests.post(url, files=files)
        response.raise_for_status()
        return response.json()  # Вернуть ответ бэкенда (если требуется)
    except requests.RequestException as e:
        logger.error(f"Ошибка при отправке данных на бэкенд: {e}")
        raise

def send_location_to_backend(latitude, longitude):
    """
    Sends latitude and longitude to the backend for validation and address lookup.
    """
    url = f"http://{ip}/locations/coordinates"
    params = {
        "latitude": latitude,
        "longitude": longitude
    }
    try:
        logger.info(params)
        response = requests.get(url, params=params)
        response.raise_for_status()
        logger.info(response.json())
        return response.json()
    except requests.RequestException as e:
        logger.error(f"Error while sending location to backend: {e}")
        return {"error": "Ошибка при проверке местоположения"}