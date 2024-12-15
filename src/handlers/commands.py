from telegram import Update, ReplyKeyboardRemove, ReplyKeyboardMarkup, KeyboardButton
from telegram.ext import ContextTypes, ConversationHandler, CommandHandler, MessageHandler, filters
from utils.logger import logger
from handlers.file_handlers import process_file, finalize_incident
from utils.api_utils import send_zip_to_backend

# States for the conversation
TITLE, DESCRIPTION = range(2)

# Structure for current incident
incident_data = {
    'files': [],
    'title': None,
    'description': None,
    'address': None,
}

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Sends a welcome message and displays available commands with a keyboard.
    """
    logger.info(f"User {update.effective_user.username} used /start")

    # Keyboard with commands
    keyboard = [
        ["/start_incident"],
        ["/finish"]
    ]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)

    commands_list = """
Добро пожаловать! Вот доступные команды:

/start_incident - Начать новый инцидент
/finish - Завершить инцидент и отправить данные

"""
    await update.message.reply_text(commands_list, reply_markup=reply_markup)

async def start_incident(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Starts the incident creation process and asks for a title.
    """
    logger.info(f"User {update.effective_user.username} started incident creation.")
    # Clear existing incident data
    incident_data['files'] = []
    incident_data['title'] = None
    incident_data['description'] = None
    incident_data['address'] = None

    keyboard = [["/cancel"]]
    
    await update.message.reply_text(
        "Введите название инцидента (например, \"Неправильная парковка\").",
        reply_markup=ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    )
    return TITLE

async def set_title(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Captures the title of the incident and asks for a description.
    """
    incident_data['title'] = update.message.text
    logger.info(f"Incident title set: {incident_data['title']}")
    # Return the keyboard with commands
    keyboard = [["/cancel"]]
    await update.message.reply_text(
        "Теперь введите описание инцидента (например, \"Машина припаркована на тротуаре\").",
        reply_markup=ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    )
    
    return DESCRIPTION

async def set_description(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Captures the description of the incident and ends the conversation.
    """
    incident_data['description'] = update.message.text
    logger.info(f"Incident description set: {incident_data['description']}")
    
    await update.message.reply_text(
        f"Инцидент сохранён:\n"
        f"Название: {incident_data['title']}\n"
        f"Описание: {incident_data['description']}\n"
        "Теперь вы можете отправить фото или видео",
    )
    
    return ConversationHandler.END

async def add_file(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Processes a photo or video sent by the user and adds it to the incident.
    """
    logger.info(f"User {update.effective_user.username} sent a file.")
    try:
        await process_file(update, incident_data)
        await update.message.reply_text("Файл успешно добавлен.")
    except Exception as e:
        logger.error(f"Error while adding file: {e}")
        await update.message.reply_text("Ошибка при добавлении файла.")

async def finish_incident(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Completes the incident creation process and returns the keyboard.
    """
    logger.info(f"User {update.effective_user.username} used /finish.")

    if not incident_data['files']:
        await update.message.reply_text("Вы не добавили ни одного файла для инцидента.")
        logger.warning("Attempt to finish an incident without files.")
        return

    # Create a ZIP archive
    try:
        zip_buffer = await finalize_incident(incident_data)
        logger.info("ZIP archive successfully created.")
    except Exception as e:
        logger.error(f"Error while creating ZIP archive: {e}")
        await update.message.reply_text("Ошибка при создании архива.")
        return

    # Send the ZIP archive to the backend
    try:
        await send_zip_to_backend(zip_buffer, incident_data)
        logger.info("Incident successfully sent to the backend.")
    except Exception as e:
        logger.error(f"Error while sending data to backend: {e}")
        await update.message.reply_text("Ошибка при отправке данных на сервер.")
        return

    # Clear incident data
    incident_data['files'].clear()
    incident_data['title'] = None
    incident_data['description'] = None
    incident_data['address'] = None
    logger.info("Incident data cleared.")

    # Return the keyboard with commands
    keyboard = [
        ["/start_incident"],
        ["/finish"]
    ]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)

    await update.message.reply_text(
        "Инцидент завершён! Вы можете начать новый инцидент или выполнить другие действия.",
        reply_markup=reply_markup
    )
    
async def request_location(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Requests the user's location with a button.
    """
    logger.info(f"User {update.effective_user.username} used /send_location.")
    
    # Кнопка для отправки геолокации
    keyboard = [[KeyboardButton("Отправить местоположение", request_location=True)]]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=True)

    await update.message.reply_text(
        "Пожалуйста, отправьте ваше местоположение, используя кнопку ниже.",
        reply_markup=reply_markup
    )

async def save_location(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Saves the user's location (latitude and longitude).
    """
    if update.message.location:
        incident_data['location'] = {
            'latitude': update.message.location.latitude,
            'longitude': update.message.location.longitude
        }
        logger.info(f"Location saved: {incident_data['location']}")
        await update.message.reply_text(
            f"Местоположение сохранено: широта {incident_data['location']['latitude']}, "
            f"долгота {incident_data['location']['longitude']}."
        )
    else:
        logger.warning("No location data received.")
        await update.message.reply_text("Ошибка: местоположение не отправлено.")


async def cancel(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Cancels the conversation.
    """
    logger.info(f"User {update.effective_user.username} cancelled the incident creation.")
    await update.message.reply_text(
        "Создание инцидента отменено.", reply_markup=ReplyKeyboardRemove()
    )
    return ConversationHandler.END
