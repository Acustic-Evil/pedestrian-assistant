from telegram import BotCommand, InlineKeyboardButton, InlineKeyboardMarkup, Update, ReplyKeyboardRemove, ReplyKeyboardMarkup, KeyboardButton
from telegram.ext import ContextTypes, ConversationHandler, CommandHandler, MessageHandler, filters
from utils.logger import logger
from handlers.file_handlers import process_file, finalize_incident
from utils.api_utils import fetch_incident_types, send_location_to_backend, send_incident_to_backend

# States for the conversation
TITLE, DESCRIPTION, SELECT_TYPE = range(3)

# Structure for current incident
incident_data = {
    'files': [],
    'title': None,
    'description': None,
    'incidentType': None,
    'address': None,
}

def generate_keyboard(minimal=False):
    """
    Generates a dynamic keyboard based on the current state of incident_data.
    If minimal=True, generates a keyboard with only /start_incident.
    """
    if minimal:
        return ReplyKeyboardMarkup([["/start_incident"]], resize_keyboard=True, one_time_keyboard=False)

    keyboard = [["/start_incident"]]
    
    if incident_data['title'] or incident_data['description'] or incident_data.get('location') or incident_data.get('files'):
        keyboard.append([KeyboardButton("Отправить местоположение", request_location=True)])
        keyboard.append(["/edit"])
        keyboard.append(["/cancel"])
        if ["/start_incident"] in keyboard:  # Проверка перед удалением
            keyboard.remove(["/start_incident"])

    # Добавляем кнопку /finish, если все поля заполнены
    if incident_data['title'] and incident_data['description'] and incident_data.get('files') and incident_data.get('address'):
        keyboard.append(["/finish"])

    return ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)



async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Sends a welcome message and displays available commands with a keyboard.
    """
    logger.info(f"User {update.effective_user.username} used /start")

    commands_list = """
Добро пожаловать! Вот доступные команды:

/start_incident - Начать новый инцидент
/finish - Завершить инцидент и отправить данные

"""
    reply_markup = generate_keyboard()
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
    incident_data['incidentType'] = None
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
    
    reply_markup = generate_keyboard()
    
    await update.message.reply_text(
        "Теперь введите описание инцидента (например, \"Машина припаркована на тротуаре\").",
        reply_markup=reply_markup
    )
    
    return DESCRIPTION

async def set_description(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Captures the description of the incident and proceeds to type selection.
    """
    incident_data['description'] = update.message.text
    logger.info(f"Incident description set: {incident_data['description']}")

    # Переход к выбору типа инцидента
    await choose_incident_type(update, context)
    return SELECT_TYPE

async def choose_incident_type(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Allows the user to select a new type of the incident for editing.
    """
    # Получаем список типов инцидентов с бэкенда
    incident_types = fetch_incident_types()  # Эта функция должна возвращать список словарей с "name" и "id"

    if not incident_types:
        # Ответ в зависимости от типа обновления
        if update.callback_query:
            await update.callback_query.answer()
            await update.callback_query.message.reply_text("Ошибка: не удалось загрузить типы инцидентов.")
        else:
            await update.message.reply_text("Ошибка: не удалось загрузить типы инцидентов.")
        return ConversationHandler.END

    # Сохраняем типы инцидентов в контексте
    context.user_data["incident_types"] = {t["name"]: t["id"] for t in incident_types}

    # Генерация клавиатуры с типами инцидентов
    keyboard = [[t["name"]] for t in incident_types] + [["/cancel"]]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)

    # Отправляем сообщение с клавиатурой
    if update.callback_query:
        await update.callback_query.answer()
        await update.callback_query.message.reply_text(
            "Выберите новый тип инцидента из предложенного списка:",
            reply_markup=reply_markup
        )
    else:
        await update.message.reply_text(
            "Выберите новый тип инцидента из предложенного списка:",
            reply_markup=reply_markup
        )

    return SELECT_TYPE



async def set_incident_type(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Captures the selected incident type and ends the conversation.
    """
    selected_type = update.message.text
    incident_types = context.user_data.get("incident_types", {})

    logger.info(f"User selected type: {selected_type}")
    logger.info(f"Available types: {incident_types}")

    # Проверяем, выбран ли тип из предложенного списка
    if selected_type in incident_types:
        incident_data["incidentType"] = {
            "id": incident_types[selected_type],
            "name": selected_type,
        }

        logger.info(f"Incident type set: {incident_data['incidentType']}")

        # Генерация обновлённой клавиатуры
        reply_markup = generate_keyboard()

        # Отправляем сообщение с подтверждением и новой клавиатурой
        await update.message.reply_text(
            f"Тип инцидента установлен: {selected_type}.\n"
            "Теперь вы можете отправить фото, видео или местоположение.",
            reply_markup=reply_markup
        )
        return ConversationHandler.END
    else:
        # Если тип не найден, отправляем сообщение об ошибке
        await update.message.reply_text("Ошибка: выберите тип инцидента из предложенного списка.")
        return SELECT_TYPE



async def add_file(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Processes a photo or video sent by the user and adds it to the incident.
    """
    logger.info(f"User {update.effective_user.username} sent a file.")
    try:
        await process_file(update, incident_data)
        reply_markup = generate_keyboard()
        await update.message.reply_text("Файл успешно добавлен.", reply_markup=reply_markup)
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
            await update.message.reply_text("Ошибка при добавлении местоположения.")
            logger.error(f"Ошибка при проверке местоположения: {backend_response['error']}")
            return

        # Сохранение местоположения и адреса
        incident_data['location'] = {
            'id': backend_response['id'],
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
    elif query.data == "edit_type":
        # Переход к выбору типа инцидента
        await query.answer()
        await choose_incident_type(update, context)
        context.user_data["editing_field"] = "incident_type"

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
        await update.message.reply_text("Местоположение будет обновлено после отправки.")
    elif editing_field == "incident_type":
        # Проверяем выбранный тип инцидента
        selected_type = update.message.text
        incident_types = context.user_data.get("incident_types", {})

        if selected_type in incident_types:
            incident_data["incidentType"] = {
                "id": incident_types[selected_type],
                "name": selected_type,
            }
            reply_markup = generate_keyboard()
            await update.message.reply_text(f"Тип инцидента обновлён: {selected_type}", reply_markup=reply_markup)
        else:
            await update.message.reply_text("Ошибка: выберите тип инцидента из предложенного списка.")
            return

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
    incident_data["incidentType"] = None
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

    # Проверяем, что incidentType — это словарь
    incident_type_data = incident_data.get("incidentType")
    if isinstance(incident_type_data, dict):
        incident_type = incident_type_data.get("name", "Тип инцидента не указан")
    else:
        incident_type = "Тип инцидента не указан"

    files_count = len(incident_data.get("files", []))

    summary = (
        f"Текущие данные инцидента:\n"
        f"Название: {title}\n"
        f"Описание: {description}\n"
        f"Местоположение: {address}\n"
        f"Тип инцидента: {incident_type}\n"
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

async def show_files_callback(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Sends each uploaded file back to the user with an option to delete.
    """
    query = update.callback_query
    await query.answer()

    if not incident_data["files"]:
        await query.edit_message_text("Файлы не добавлены.")
        return

    # Отправляем каждое фото/видео пользователю с кнопками
    for idx, file in enumerate(incident_data["files"]):
        keyboard = InlineKeyboardMarkup([
            [InlineKeyboardButton(f"Удалить этот файл", callback_data=f"delete_file_{idx}")]
        ])

        try:
            # Если это фото
            if file.file_id and file.file_path.endswith(('.jpg', '.png')):
                await context.bot.send_photo(
                    chat_id=query.message.chat_id,
                    photo=file.file_id,
                    reply_markup=keyboard
                )
            # Если это видео
            elif file.file_id and file.file_path.endswith('.mp4'):
                await context.bot.send_video(
                    chat_id=query.message.chat_id,
                    video=file.file_id,
                    reply_markup=keyboard
                )
            else:
                await context.bot.send_message(
                    chat_id=query.message.chat_id,
                    text=f"Неизвестный формат файла: {file.file_id}"
                )
        except Exception as e:
            logger.error(f"Ошибка отправки файла: {e}")
            await context.bot.send_message(
                chat_id=query.message.chat_id,
                text=f"Не удалось отправить файл {idx + 1}."
            )

    # Кнопка для удаления всех файлов
    delete_all_keyboard = InlineKeyboardMarkup([
        [InlineKeyboardButton("Удалить все файлы", callback_data="delete_all_files")]
    ])
    await context.bot.send_message(
        chat_id=query.message.chat_id,
        text="Выберите действие для всех файлов:",
        reply_markup=delete_all_keyboard
    )


async def delete_file(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Handles the deletion of a single file or all files.
    """
    query = update.callback_query
    await query.answer()

    if query.data.startswith("delete_file_"):
        file_idx = int(query.data.split("_")[-1])

        if 0 <= file_idx < len(incident_data["files"]):
            deleted_file = incident_data["files"].pop(file_idx)
            logger.info(f"File deleted: {deleted_file.file_id}")
            await query.message.reply_text(f"Файл {file_idx} удалён.")
        else:
            await query.message.reply_text("Ошибка: файл не найден.")

    elif query.data == "delete_all_files":
        incident_data["files"].clear()
        logger.info("All files deleted.")
        await query.message.reply_text("Все файлы удалены.")


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
        [InlineKeyboardButton("Редактировать тип инцидента", callback_data="edit_type")],
        [InlineKeyboardButton("Редактировать файлы", callback_data="show_files")]
    ]
    reply_markup = InlineKeyboardMarkup(keyboard)

    await update.message.reply_text(summary, reply_markup=reply_markup)
    
async def request_location(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Requests the user's location with a button.
    """
    logger.info(f"User {update.effective_user.username} used /send_location.")
    
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
    if not incident_data["title"] or not incident_data["description"] or not incident_data.get("files"):
        await query.edit_message_text("Ошибка: не все данные заполнены.")
        return

    await query.edit_message_text("Отправляем данные...")

    # Логика отправки данных
    try:
        zip_buffer = await finalize_incident(incident_data)
        await send_incident_to_backend(zip_buffer, incident_data, update)
        await query.edit_message_text("Инцидент успешно отправлен!")
        logger.info("Incident successfully sent to the backend.")

        # Очистка данных
        incident_data['files'].clear()
        incident_data['title'] = None
        incident_data['description'] = None
        incident_data['address'] = None
        incident_data["incidentType"] = None
        logger.info("Incident data cleared.")

        # Генерация минимальной клавиатуры
        reply_markup = generate_keyboard(minimal=True)

        # Отправляем новое сообщение с клавиатурой
        await query.message.reply_text("Что вы хотите сделать дальше?", reply_markup=reply_markup)

    except Exception as e:
        logger.error(f"Error while sending data: {e}")
        await query.edit_message_text("Ошибка при отправке данных.")

