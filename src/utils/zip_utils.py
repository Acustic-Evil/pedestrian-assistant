import zipfile
from io import BytesIO

async def create_zip_in_memory(files):
    """
    Создаёт ZIP-архив в памяти из списка объектов File.
    """
    zip_buffer = BytesIO()
    with zipfile.ZipFile(zip_buffer, 'w') as zipf:
        for file in files:
            file_data = await file.download_as_bytearray()
            filename = f"{file.file_id}.{'jpg' if file.file_path.endswith('.jpg') else 'mp4'}"
            zipf.writestr(filename, file_data)
    zip_buffer.seek(0)
    return zip_buffer
