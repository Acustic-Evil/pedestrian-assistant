from venv import logger


def validate_file(file, file_path, max_size_mb):
    """
    Checks the validity of the file by size and type.
    :param file: A file object from Telegram.
    :param file_path: Path or file name.
    :param max_size_mb: The maximum allowed file size (in MB).
    """
    # Проверка размера файла
    if file.file_size > max_size_mb * 1024 * 1024:
        logger.warning(f"File size {file.file_id} exceeds {max_size_mb} MB.")
        return False
    
    # Проверка расширения файла
    valid_extensions = ['.jpg', '.png', '.mp4']
    if not any(file_path.endswith(ext) for ext in valid_extensions):
        logger.warning(f"Invalid file format {file.file_id}.")
        return False

    return True
