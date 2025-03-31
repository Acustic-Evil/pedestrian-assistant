import zipfile
from io import BytesIO
from utils.logger import logger

async def create_zip_in_memory(files):
    """
    Создаёт ZIP-архив в памяти из списка объектов File или словарей с метаданными.
    """
    zip_buffer = BytesIO()
    with zipfile.ZipFile(zip_buffer, 'w') as zipf:
        for file_data in files:
            try:
                # Определяем, работаем ли мы со словарем метаданных или с объектом File
                if isinstance(file_data, dict) and 'file' in file_data:
                    file = file_data['file']
                    file_id = file_data.get('file_id')
                    file_path = file_data.get('file_path')
                else:
                    # Обратная совместимость со старым форматом
                    file = file_data
                    file_id = getattr(file, 'file_id', 'unknown')
                    file_path = getattr(file, 'file_path', None)
                
                # Скачиваем файл
                file_data_bytes = await file.download_as_bytearray()
                
                # Определяем имя файла
                if file_path:
                    extension = '.jpg' if file_path.endswith('.jpg') else '.mp4'
                else:
                    extension = '.bin'
                filename = f"{file_id}{extension}"
                
                # Добавляем файл в архив
                zipf.writestr(filename, file_data_bytes)
                logger.info(f"Файл {filename} добавлен в архив")
            except Exception as e:
                file_id = file_data.get('file_id') if isinstance(file_data, dict) else getattr(file_data, 'file_id', 'unknown')
                logger.error(f"Ошибка при добавлении файла {file_id} в архив: {e}")
                raise
    zip_buffer.seek(0)
    return zip_buffer
