import requests
from utils.logger import logger

async def send_zip_to_backend(zip_buffer, incident_data):
    """
    Отправляет ZIP-архив на бэкенд с дополнительными данными инцидента.
    """
    url = "http://localhost:8080/api/media/upload"
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
        logger.info(f"Ответ от бэкенда: {response.status_code}, {response.text}")
    except requests.RequestException as e:
        logger.error(f"Ошибка при отправке на бэкенд: {e}")
        raise
