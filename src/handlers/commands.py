import logging
import requests
from telegram import BotCommand, InlineKeyboardButton, InlineKeyboardMarkup, Update, ReplyKeyboardRemove, ReplyKeyboardMarkup, KeyboardButton
from telegram.ext import ContextTypes, ConversationHandler, CommandHandler, MessageHandler, filters
from utils.logger import logger
from handlers.file_handlers import process_file, finalize_incident
from utils.api_utils import send_location_to_backend, send_zip_to_backend

# States for the conversation
TITLE, DESCRIPTION = range(2)

# Structure for current incident
incident_data = {
    'files': [],
    'title': None,
    'description': None,
    'address': None,
}

def generate_keyboard():
    """
    Generates a dynamic keyboard based on the current state of incident_data.
    """
    keyboard = [["/start_incident"]]
    
    if incident_data['title'] or incident_data['description'] or incident_data.get('location') or incident_data.get('files'):
        keyboard.append([KeyboardButton("Отправить местоположение", request_location=True)])
        keyboard.append(["/edit"])
        keyboard.append(["/cancel"])
        keyboard.remove(["/start_incident"])

    # Добавляем кнопку /finish, если все поля заполнены
    if incident_data['title'] and incident_data['description'] and incident_data.get('location') and incident_data.get('files'):
        keyboard.append(["/finish"])

    return ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)


async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Sends a welcome message and displays available commands with a keyboard.
    """
    logger.info(f"User {update.effective_user.username} used /start")

    # # Keyboard with commands
    # keyboard = [
    #     ["/start_incident"]
    # ]
    # reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)

    commands_list = """
Добро пожаловать! Вот доступные команды:

/start_incident - Начать новый инцидент
/finish - Завершить инцидент и отправить данные

"""
    reply_markup = generate_keyboard()
    # await update.message.reply_text(commands_list, reply_markup=reply_markup)
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
    # keyboard = [["/cancel"]]
    # await update.message.reply_text(
    #     "Теперь введите описание инцидента (например, \"Машина припаркована на тротуаре\").",
    #     reply_markup=ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    # )
    
    reply_markup = generate_keyboard()
    
    await update.message.reply_text(
        "Теперь введите описание инцидента (например, \"Машина припаркована на тротуаре\").",
        reply_markup=reply_markup
    )
    
    return DESCRIPTION

async def set_description(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Captures the description of the incident and ends the conversation.
    """
    incident_data['description'] = update.message.text
    logger.info(f"Incident description set: {incident_data['description']}")

    # Проверяем, нужно ли показывать кнопку /finish
    reply_markup = generate_keyboard()

    await update.message.reply_text(
        f"Инцидент сохранён:\n"
        f"Название: {incident_data['title']}\n"
        f"Описание: {incident_data['description']}\n"
        "Теперь вы можете отправить фото или видео и местополложение.",
        reply_markup=reply_markup
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

async def save_location(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Saves the user's location (latitude and longitude) and verifies it via the backend.
    """
    if update.message.location:
        latitude = update.message.location.latitude
        longitude = update.message.location.longitude

        # Отправка координат на бэкенд для проверки
        backend_response = send_location_to_backend(latitude, longitude)

        # Проверка ответа от бэкенда
        if "error" in backend_response:
            logger.error(f"Ошибка при проверке местоположения: {backend_response['error']}")
            return

        # Сохранение местоположения и адреса
        incident_data['location'] = {
            'latitude': latitude,
            'longitude': longitude
        }
        incident_data['address'] = backend_response.get("address", "Адрес не найден")

        # Проверяем, нужно ли показывать кнопку /finish
        reply_markup = generate_keyboard()

        await update.message.reply_text(
            f"Местоположение сохранено: широта {latitude}, долгота {longitude}.\n"
            f"Адрес: {incident_data['address']}",
            reply_markup=reply_markup
        )
    else:
        await update.message.reply_text("Ошибка: местоположение не отправлено.")

async def edit_field(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Handles editing of incident fields based on user selection.
    """
    query = update.callback_query
    await query.answer()

    # Определяем, что редактировать
    if query.data == "edit_title":
        await query.edit_message_text("Введите новое название инцидента:")
        context.user_data["editing_field"] = "title"
    elif query.data == "edit_description":
        await query.edit_message_text("Введите новое описание инцидента:")
        context.user_data["editing_field"] = "description"
    elif query.data == "edit_location":
        await query.edit_message_text("Пожалуйста, отправьте новое местоположение.")
        context.user_data["editing_field"] = "location"
    elif query.data == "show_files":
        show_files

async def save_edited_field(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Saves the edited field to the incident data.
    """
    editing_field = context.user_data.get("editing_field")
    if not editing_field:
        await update.message.reply_text("Ошибка: нет редактируемого поля.")
        return

    if editing_field == "title":
        incident_data["title"] = update.message.text
        await update.message.reply_text(f"Название обновлено: {incident_data['title']}")
    elif editing_field == "description":
        incident_data["description"] = update.message.text
        await update.message.reply_text(f"Описание обновлено: {incident_data['description']}")
    elif editing_field == "location":
        # Локация сохраняется автоматически через другой обработчик
        await update.message.reply_text("Местоположение будет обновлено после отправки.")
    
    # Удаляем флаг редактирования
    context.user_data["editing_field"] = None

    # Показываем обновлённое сообщение с данными
    summary = generate_summary_message()
    await update.message.reply_text(summary)


async def cancel(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Cancels the conversation.
    """
    incident_data['files'].clear()
    incident_data['title'] = None
    incident_data['description'] = None
    incident_data['address'] = None
    logger.info("Incident data cleared.")
    
    keyboard = [
        ["/start_incident"],
    ]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    logger.info(f"User {update.effective_user.username} cancelled the incident creation.")
    await update.message.reply_text(
        "Создание инцидента отменено.", reply_markup=reply_markup
    )
    return ConversationHandler.END


def generate_summary_message():
    """
    Generates a summary of the current incident data.
    """
    title = incident_data.get("title", "Название не указано")
    description = incident_data.get("description", "Описание не указано")
    address = incident_data.get("address", "Местоположение не указано")
    files_count = len(incident_data.get("files", []))

    summary = (
        f"Текущие данные инцидента:\n"
        f"Название: {title}\n"
        f"Описание: {description}\n"
        f"Местоположение: {address}\n"
        f"Количество файлов: {files_count}\n"
    )
    return summary

def generate_file_keyboard():
    """
    Generates an inline keyboard with options to delete individual files or all files.
    """
    keyboard = []

    # Создаём кнопки для каждого файла
    for idx, file in enumerate(incident_data["files"]):
        keyboard.append([
            InlineKeyboardButton(f"Удалить файл {idx + 1}", callback_data=f"delete_file_{idx}")
        ])

    # Кнопка для удаления всех файлов
    if incident_data["files"]:
        keyboard.append([InlineKeyboardButton("Удалить все файлы", callback_data="delete_all_files")])

    return InlineKeyboardMarkup(keyboard)

async def show_files(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Displays the list of uploaded files with options to delete.
    """
    if not incident_data["files"]:
        await update.message.reply_text("Файлы не добавлены.")
        return

    # Генерация сообщения с файлами
    file_list = "\n".join([f"{idx + 1}. {file.file_id}" for idx, file in enumerate(incident_data["files"])])
    keyboard = generate_file_keyboard()

    await update.message.reply_text(
        f"Загруженные файлы:\n{file_list}\n\nВыберите действие:",
        reply_markup=keyboard
    )
    
async def delete_file(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Handles the deletion of individual or all files based on the user's selection.
    """
    query = update.callback_query
    await query.answer()

    # Обработка удаления конкретного файла
    if query.data.startswith("delete_file_"):
        file_idx = int(query.data.split("_")[-1])

        if 0 <= file_idx < len(incident_data["files"]):
            deleted_file = incident_data["files"].pop(file_idx)
            logger.info(f"File deleted: {deleted_file.file_id}")
            await query.edit_message_text(f"Файл {file_idx + 1} удалён.")
        else:
            await query.edit_message_text("Ошибка: файл не найден.")

    # Обработка удаления всех файлов
    elif query.data == "delete_all_files":
        incident_data["files"].clear()
        logger.info("All files deleted.")
        await query.edit_message_text("Все файлы удалены.")

    # Обновляем список файлов
    if incident_data["files"]:
        file_list = "\n".join([f"{idx + 1}. {file.file_id}" for idx, file in enumerate(incident_data["files"])])
        keyboard = generate_file_keyboard()
        await query.message.reply_text(
            f"Оставшиеся файлы:\n{file_list}\n\nВыберите действие:",
            reply_markup=keyboard
        )

async def finish_incident(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Shows the current data and gives ability to edit it before sending
    """
    logger.info(f"User {update.effective_user.username} used /finish_incident.")

    # Генерируем сообщение с текущими данными
    summary = generate_summary_message()

    # Кнопки для редактирования и подтверждения
    keyboard = [
        [InlineKeyboardButton("Редактировать название", callback_data="edit_title")],
        [InlineKeyboardButton("Редактировать описание", callback_data="edit_description")],
        [InlineKeyboardButton("Редактировать местоположение", callback_data="edit_location")],
        [InlineKeyboardButton("Редактировать файлы", callback_data="show_files")],
        [InlineKeyboardButton("Отправить инцидент", callback_data="confirm_send")],
    ]
    reply_markup = InlineKeyboardMarkup(keyboard)

    await update.message.reply_text(summary, reply_markup=reply_markup)
    
async def edit_data_request(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Shows the current incident data and options to edit fields or send the data.
    """
    # Генерируем сообщение с текущими данными
    summary = generate_summary_message()

    # Кнопки для редактирования и подтверждения
    keyboard = [
        [InlineKeyboardButton("Редактировать название", callback_data="edit_title")],
        [InlineKeyboardButton("Редактировать описание", callback_data="edit_description")],
        [InlineKeyboardButton("Редактировать местоположение", callback_data="edit_location")],
        [InlineKeyboardButton("Редактировать файлы", callback_data="show_files")]
    ]
    reply_markup = InlineKeyboardMarkup(keyboard)

    await update.message.reply_text(summary, reply_markup=reply_markup)
    
async def request_location(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Requests the user's location with a button.
    """
    logger.info(f"User {update.effective_user.username} used /send_location.")
    
    # # Кнопка для отправки геолокации
    # keyboard = [[KeyboardButton("Отправить местоположение", request_location=True)]]
    # reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=True)
    reply_markup = generate_keyboard()
    await update.message.reply_text(
        "Пожалуйста, отправьте ваше местоположение, используя кнопку ниже.",
        reply_markup=reply_markup
    )
    
async def confirm_send(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Sends the incident data to the backend.
    """
    query = update.callback_query
    await query.answer()

    # Проверяем, заполнены ли все поля
    if not incident_data["title"] or not incident_data["description"] or not incident_data.get("location"):
        await query.edit_message_text("Ошибка: не все данные заполнены.")
        return

    await query.edit_message_text("Отправляем данные...")

    # Логика отправки данных
    try:
        zip_buffer = await finalize_incident(incident_data)
        await send_zip_to_backend(zip_buffer, incident_data)
        await query.edit_message_text("Инцидент успешно отправлен!")
        logger.info("Incident successfully sent to the backend.")
    except Exception as e:
        logger.error(f"Error while sending data: {e}")
        await query.edit_message_text("Ошибка при отправке данных.")
