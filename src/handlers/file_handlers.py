from utils.logger import logger
from handlers.validation import validate_file
from utils.zip_utils import create_zip_in_memory

MAX_FILES = 5  # Максимальное количество файлов на инцидент
MAX_PHOTO_SIZE_MB = 10  # Максимальный размер фото (в МБ)
MAX_VIDEO_SIZE_MB = 20  # Максимальный размер видео (в МБ)

async def process_file(update, incident_data):
    """
    Обрабатывает входящие фото или видео, проверяет их валидность и добавляет в список инцидента.
    """
    # Определяем тип файла (фото или видео) и формируем file_path
    if update.message.photo:
        file = await update.message.photo[-1].get_file()
        file_path = f"{file.file_id}.jpg"
        file_type = "photo"
        max_size_mb = MAX_PHOTO_SIZE_MB
    elif update.message.video:
        file = await update.message.video.get_file()
        file_path = f"{file.file_id}.mp4"
        file_type = "video"
        max_size_mb = MAX_VIDEO_SIZE_MB
    else:
        logger.warning("Получен неподдерживаемый тип файла.")
        raise ValueError("Поддерживаются только фото и видео.")

    # Проверка на максимальное количество файлов
    if len(incident_data['files']) >= MAX_FILES:
        logger.warning("Превышен лимит файлов для инцидента.")
        raise ValueError(f"Превышен лимит файлов. Максимум: {MAX_FILES}.")

    # Проверка валидности файла
    if not validate_file(file, file_path, max_size_mb):
        logger.warning(f"Файл {file.file_id} не прошёл валидацию.")
        raise ValueError("Файл не соответствует ограничениям.")

    # Добавление файла в инцидент
    incident_data['files'].append(file)
    logger.info(f"{file_type.capitalize()} добавлено: {file.file_id}")

async def finalize_incident(incident_data):
    """
    Создаёт ZIP-архив в памяти из файлов инцидента.
    """
    try:
        zip_buffer = await create_zip_in_memory(incident_data['files'])
        return zip_buffer
    except Exception as e:
        logger.error(f"Ошибка при создании ZIP-архива: {e}")
        raise