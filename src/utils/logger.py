import logging

# Конфигурация логгера
logging.basicConfig(
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.INFO,  # Измените на DEBUG для более подробных логов
    handlers=[
        logging.FileHandler("bot.log"),  # Логи в файл
        logging.StreamHandler()         # Логи в консоль
    ]
)

logger = logging.getLogger(__name__)
