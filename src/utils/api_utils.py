import os
from dotenv import load_dotenv
import requests
from utils.logger import logger


load_dotenv()
ip = os.getenv("HOST_IP")

async def send_zip_to_backend(zip_buffer, incident_data):
    url = f"http://{ip}/api/media/upload"
    files = {
        "zip-file": ("incident_media.zip", zip_buffer, "application/zip")
    }
    # data = {
    #     "title": incident_data['title'],
    #     "description": incident_data['description'],
    #     "address": incident_data['address']
    # }

    try:
        # response = requests.post(url, files=files, data=data)
        response = requests.post(url, files=files)
        response.raise_for_status()
        logger.info(f"Response {response.status_code}, {response.text}")
    except requests.RequestException as e:
        logger.error(f"Ошибка при отправке на бэкенд: {e}")
        raise

def send_location_to_backend(latitude, longitude):
    """
    Sends latitude and longitude to the backend for validation and address lookup.
    """
    url = f"http://{ip}/geocode"  # Замените на URL вашего бэкенда
    params = {
        "latitude": latitude,
        "longitude": longitude
    }
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()  # Ожидается, что бэкенд возвращает JSON с адресом
    except requests.RequestException as e:
        logger.error(f"Error while sending location to backend: {e}")
        return {"error": "Ошибка при проверке местоположения"}