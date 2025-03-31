from telegram import BotCommand, InlineKeyboardButton, InlineKeyboardMarkup, Update, ReplyKeyboardRemove, ReplyKeyboardMarkup, KeyboardButton
from telegram.ext import ContextTypes, ConversationHandler, CommandHandler, MessageHandler, filters
from utils.logger import logger
from handlers.file_handlers import process_file, finalize_incident
from utils.api_utils import fetch_incident_types, send_location_to_backend, send_incident_to_backend

# States for the conversation
TITLE, DESCRIPTION, SELECT_TYPE = range(3)

# Structure for current incident will be stored in context.user_data['incident']

def generate_keyboard(context=None, minimal=False):
    """
    Generates a dynamic keyboard based on the current state of context.user_data['incident'].
    If minimal=True, generates a keyboard with only /start_incident.
    """
    if minimal:
        return ReplyKeyboardMarkup([["🚀 Начать новый инцидент"]], resize_keyboard=True, one_time_keyboard=False)

    keyboard = [["🚀 Начать новый инцидент"]]
    
    # Если контекст не передан, возвращаем базовую клавиатуру
    if not context or 'incident' not in context.user_data:
        return ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    
    incident_data = context.user_data['incident']
    
    # Если есть какие-то данные инцидента, показываем соответствующие кнопки
    if incident_data.get('title') or incident_data.get('description') or incident_data.get('location') or incident_data.get('files'):
        keyboard = []
        keyboard.append([KeyboardButton("📍 Отправить местоположение", request_location=True)])
        
        # Добавляем кнопки для управления инцидентом
        incident_buttons = []
        if incident_data.get('files'):
            incident_buttons.append("📁 Файлы")
        incident_buttons.append("✏️ Редактировать")
        incident_buttons.append("❌ Отменить")
        keyboard.append(incident_buttons)
        
        # Добавляем кнопку отправки, если все необходимые поля заполнены
        if incident_data.get('title') and incident_data.get('description') and incident_data.get('files') and incident_data.get('address'):
            keyboard.append(["✅ Отправить инцидент"])

    return ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)



async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Sends a welcome message and displays available commands with a keyboard.
    """
    logger.info(f"User {update.effective_user.username} used /start")

    commands_list = """
Добро пожаловать в помощник пешехода! 👋

Нажмите кнопку "🚀 Начать новый инцидент" для создания сообщения о нарушении.
"""
    reply_markup = generate_keyboard()
    await update.message.reply_text(commands_list, reply_markup=reply_markup)

async def start_incident(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Starts the incident creation process and asks for a title.
    """
    logger.info(f"User {update.effective_user.username} started incident creation.")
    # Initialize or clear existing incident data
    context.user_data['incident'] = {
        'files': [],
        'title': None,
        'description': None,
        'incidentType': None,
        'address': None
    }

    keyboard = [["❌ Отменить"]]
    
    await update.message.reply_text(
        "📝 Введите название инцидента (например, \"Неправильная парковка\").",
        reply_markup=ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    )
    
    return TITLE

async def set_title(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Captures the title of the incident and asks for a description.
    """
    context.user_data['incident']['title'] = update.message.text
    logger.info(f"Incident title set: {context.user_data['incident']['title']}")
    
    keyboard = [["❌ Отменить"]]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    
    await update.message.reply_text(
        "✅ Название сохранено! Теперь введите описание инцидента (например, \"Машина припаркована на тротуаре\").",
        reply_markup=reply_markup
    )
    
    return DESCRIPTION

async def set_description(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Captures the description of the incident and proceeds to type selection.
    """
    context.user_data['incident']['description'] = update.message.text
    logger.info(f"Incident description set: {context.user_data['incident']['description']}")

    # Сообщаем пользователю, что описание сохранено
    await update.message.reply_text("✅ Описание сохранено! Теперь выберите тип инцидента.")
    
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
    keyboard = [[t["name"]] for t in incident_types] + [["❌ Отменить"]]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)

    # Отправляем сообщение с клавиатурой
    if update.callback_query:
        await update.callback_query.answer()
        await update.callback_query.message.reply_text(
            "🔍 Выберите тип инцидента из списка ниже:",
            reply_markup=reply_markup
        )
    else:
        await update.message.reply_text(
            "🔍 Выберите тип инцидента из списка ниже:",
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
    if selected_type == "❌ Отменить":
        return await cancel(update, context)
    elif selected_type in incident_types:
        context.user_data['incident']["incidentType"] = {
            "id": incident_types[selected_type],
            "name": selected_type,
        }

        logger.info(f"Incident type set: {context.user_data['incident']['incidentType']}")

        # Генерация обновлённой клавиатуры
        reply_markup = generate_keyboard(context)

        # Отправляем сообщение с подтверждением и новой клавиатурой
        await update.message.reply_text(
            f"✅ Тип инцидента установлен: {selected_type}\n\n"
            "📸 Теперь отправьте фото или видео нарушения\n"
            "📍 Также не забудьте отправить местоположение",
            reply_markup=reply_markup
        )
        return ConversationHandler.END
    else:
        # Если тип не найден, отправляем сообщение об ошибке
        await update.message.reply_text("⚠️ Пожалуйста, выберите тип инцидента из предложенного списка.")
        return SELECT_TYPE



async def add_file(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Processes a photo or video sent by the user and adds it to the incident.
    """
    logger.info(f"User {update.effective_user.username} sent a file.")
    try:
        # Проверяем, инициализирован ли incident в user_data
        if 'incident' not in context.user_data:
            context.user_data['incident'] = {
                'files': [],
                'title': None,
                'description': None,
                'incidentType': None,
                'address': None
            }
        await process_file(update, context.user_data['incident'])
        reply_markup = generate_keyboard(context)
        files_count = len(context.user_data['incident'].get("files", []))
        await update.message.reply_text(f"✅ Файл успешно добавлен! (Всего файлов: {files_count})", reply_markup=reply_markup)
    except Exception as e:
        logger.error(f"Error while adding file: {e}")
        await update.message.reply_text("⚠️ Ошибка при добавлении файла. Пожалуйста, попробуйте еще раз.")

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
            await update.message.reply_text("⚠️ Не удалось определить местоположение. Пожалуйста, попробуйте еще раз.")
            logger.error(f"Ошибка при проверке местоположения: {backend_response['error']}")
            return

        # Сохранение местоположения и адреса
        # Проверяем, инициализирован ли incident в user_data
        if 'incident' not in context.user_data:
            context.user_data['incident'] = {
                'files': [],
                'title': None,
                'description': None,
                'incidentType': None,
                'address': None
            }
            
        context.user_data['incident']['location'] = {
            'id': backend_response['id'],
            'latitude': latitude,
            'longitude': longitude
        }
        context.user_data['incident']['address'] = backend_response.get("address", "Адрес не найден")

        # Проверяем, нужно ли показывать кнопку отправки
        reply_markup = generate_keyboard(context)

        await update.message.reply_text(
            f"📍 Местоположение сохранено!\n"
            f"📌 Адрес: {context.user_data['incident']['address']}",
            reply_markup=reply_markup
        )
        
        # Если все необходимые данные заполнены, подсказываем пользователю следующий шаг
        if context.user_data['incident'].get('title') and context.user_data['incident'].get('description') and context.user_data['incident'].get('files') and context.user_data['incident'].get('address'):
            await update.message.reply_text(
                "✅ Все необходимые данные заполнены! Нажмите кнопку 'Отправить инцидент' для завершения.",
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
        await query.edit_message_text("✏️ Введите новое название инцидента:")
        context.user_data["editing_field"] = "title"
    elif query.data == "edit_description":
        await query.edit_message_text("✏️ Введите новое описание инцидента:")
        context.user_data["editing_field"] = "description"
    elif query.data == "edit_location":
        await query.edit_message_text("📍 Пожалуйста, отправьте новое местоположение, используя кнопку в клавиатуре.")
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
        await update.message.reply_text("⚠️ Нет активного поля для редактирования.")
        return

    if editing_field == "title":
        context.user_data['incident']["title"] = update.message.text
        await update.message.reply_text(f'✅ Название успешно обновлено: "{context.user_data["incident"]["title"]}"')
    elif editing_field == "description":
        context.user_data['incident']["description"] = update.message.text
        await update.message.reply_text(f'✅ Описание успешно обновлено: "{context.user_data["incident"]["description"]}"')
    elif editing_field == "location":
        await update.message.reply_text("📍 Пожалуйста, используйте кнопку 'Отправить местоположение' в клавиатуре.")
    elif editing_field == "incident_type":
        # Проверяем выбранный тип инцидента
        selected_type = update.message.text
        incident_types = context.user_data.get("incident_types", {})

        if selected_type in incident_types:
            context.user_data['incident']["incidentType"] = {
                "id": incident_types[selected_type],
                "name": selected_type,
            }
            reply_markup = generate_keyboard(context)
            await update.message.reply_text(f"✅ Тип инцидента успешно обновлён: {selected_type}", reply_markup=reply_markup)
        else:
            await update.message.reply_text("⚠️ Пожалуйста, выберите тип инцидента из предложенного списка.")
            return

    # Удаляем флаг редактирования
    context.user_data["editing_field"] = None

    # Показываем обновлённое сообщение с данными
    summary = generate_summary_message()
    reply_markup = generate_keyboard()
    await update.message.reply_text("📋 Обновленные данные инцидента:\n\n" + summary, reply_markup=reply_markup)




async def cancel(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Cancels the conversation.
    """
    # Очищаем данные инцидента в контексте пользователя
    if 'incident' in context.user_data:
        context.user_data['incident'] = {
            'files': [],
            'title': None,
            'description': None,
            'incidentType': None,
            'address': None
        }
    logger.info("Incident data cleared.")
    
    keyboard = [
        ["🚀 Начать новый инцидент"],
    ]
    reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=False)
    logger.info(f"User {update.effective_user.username} cancelled the incident creation.")
    await update.message.reply_text(
        "❌ Создание инцидента отменено. Вы можете начать заново, когда будете готовы.", reply_markup=reply_markup
    )
    return ConversationHandler.END


def generate_summary_message(context=None):
    """
    Generates a summary of the current incident data.
    """
    if not context or 'incident' not in context.user_data:
        return "Нет данных об инциденте."
        
    incident_data = context.user_data['incident']
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


def generate_file_keyboard(context):
    """
    Generates an inline keyboard with options to delete individual files or all files.
    """
    keyboard = []

    # Проверяем, инициализирован ли incident в user_data
    if 'incident' not in context.user_data:
        return InlineKeyboardMarkup(keyboard)

    # Создаём кнопки для каждого файла
    for idx, file in enumerate(context.user_data['incident']["files"]):
        keyboard.append([
            InlineKeyboardButton(f"Удалить файл {idx + 1}", callback_data=f"delete_file_{idx}")
        ])

    # Кнопка для удаления всех файлов
    if context.user_data['incident']["files"]:
        keyboard.append([InlineKeyboardButton("Удалить все файлы", callback_data="delete_all_files")])

    return InlineKeyboardMarkup(keyboard)

async def show_files(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Handles the "📁 Файлы" button press from the main keyboard.
    Shows all uploaded files with options to delete them.
    """
    logger.info(f"User {update.effective_user.username} requested to view files.")
    
    # Проверяем, инициализирован ли incident в user_data
    if 'incident' not in context.user_data or not context.user_data['incident']["files"]:
        await update.message.reply_text("Файлы не добавлены. Отправьте фото или видео, чтобы добавить файлы.")
        return
    
    # Отправляем каждое фото/видео пользователю с кнопками
    for idx, file in enumerate(context.user_data['incident']["files"]):
        keyboard = InlineKeyboardMarkup([
            [InlineKeyboardButton(f"Удалить этот файл", callback_data=f"delete_file_{idx}")]
        ])

        try:
            # Проверяем тип файла в словаре метаданных
            if isinstance(file, dict) and 'file_type' in file:
                file_id = file.get('file_id')
                if file['file_type'] == 'photo':
                    await context.bot.send_photo(
                        chat_id=update.message.chat_id,
                        photo=file_id,
                        reply_markup=keyboard
                    )
                elif file['file_type'] == 'video':
                    await context.bot.send_video(
                        chat_id=update.message.chat_id,
                        video=file_id,
                        reply_markup=keyboard
                    )
                else:
                    await context.bot.send_message(
                        chat_id=update.message.chat_id,
                        text=f"Неизвестный формат файла: {file_id}"
                    )
            # Запасной вариант - проверка по расширению файла
            elif isinstance(file, dict) and 'file_path' in file:
                file_id = file.get('file_id')
                if file['file_path'].endswith(('.jpg', '.png')):
                    await context.bot.send_photo(
                        chat_id=update.message.chat_id,
                        photo=file_id,
                        reply_markup=keyboard
                    )
                elif file['file_path'].endswith('.mp4'):
                    await context.bot.send_video(
                        chat_id=update.message.chat_id,
                        video=file_id,
                        reply_markup=keyboard
                    )
                else:
                    await context.bot.send_message(
                        chat_id=update.message.chat_id,
                        text=f"Неизвестный формат файла: {file_id}"
                    )
            # Обратная совместимость со старым форматом
            elif hasattr(file, 'file_id'):
                file_id = file.file_id
                logger.warning(f"Файл в старом формате: {file_id}")
                await context.bot.send_message(
                    chat_id=update.message.chat_id,
                    text=f"Неизвестный формат файла: {file_id}"
                )
        except Exception as e:
            logger.error(f"Ошибка отправки файла: {e}")
            await context.bot.send_message(
                chat_id=update.message.chat_id,
                text=f"Не удалось отправить файл {idx + 1}."
            )

    # Кнопка для удаления всех файлов
    delete_all_keyboard = InlineKeyboardMarkup([
        [InlineKeyboardButton("Удалить все файлы", callback_data="delete_all_files")]
    ])
    await context.bot.send_message(
        chat_id=update.message.chat_id,
        text="Выберите действие для всех файлов:",
        reply_markup=delete_all_keyboard
    )

async def show_files(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Handles the "📁 Файлы" button press from the main keyboard.
    Shows all uploaded files with options to delete them.
    """
    logger.info(f"User {update.effective_user.username} requested to view files.")
    
    # Проверяем, инициализирован ли incident в user_data
    if 'incident' not in context.user_data or not context.user_data['incident']["files"]:
        await update.message.reply_text("Файлы не добавлены. Отправьте фото или видео, чтобы добавить файлы.")
        return
    
    # Отправляем каждое фото/видео пользователю с кнопками
    for idx, file in enumerate(context.user_data['incident']["files"]):
        keyboard = InlineKeyboardMarkup([
            [InlineKeyboardButton(f"Удалить этот файл", callback_data=f"delete_file_{idx}")]
        ])

        try:
            # Проверяем тип файла в словаре метаданных
            if isinstance(file, dict) and 'file_type' in file:
                file_id = file.get('file_id')
                if file['file_type'] == 'photo':
                    await context.bot.send_photo(
                        chat_id=update.message.chat_id,
                        photo=file_id,
                        reply_markup=keyboard
                    )
                elif file['file_type'] == 'video':
                    await context.bot.send_video(
                        chat_id=update.message.chat_id,
                        video=file_id,
                        reply_markup=keyboard
                    )
                else:
                    await context.bot.send_message(
                        chat_id=update.message.chat_id,
                        text=f"Неизвестный формат файла: {file_id}"
                    )
            # Запасной вариант - проверка по расширению файла
            elif isinstance(file, dict) and 'file_path' in file:
                file_id = file.get('file_id')
                if file['file_path'].endswith(('.jpg', '.png')):
                    await context.bot.send_photo(
                        chat_id=update.message.chat_id,
                        photo=file_id,
                        reply_markup=keyboard
                    )
                elif file['file_path'].endswith('.mp4'):
                    await context.bot.send_video(
                        chat_id=update.message.chat_id,
                        video=file_id,
                        reply_markup=keyboard
                    )
                else:
                    await context.bot.send_message(
                        chat_id=update.message.chat_id,
                        text=f"Неизвестный формат файла: {file_id}"
                    )
            # Обратная совместимость со старым форматом
            elif hasattr(file, 'file_id'):
                file_id = file.file_id
                logger.warning(f"Файл в старом формате: {file_id}")
                await context.bot.send_message(
                    chat_id=update.message.chat_id,
                    text=f"Неизвестный формат файла: {file_id}"
                )
        except Exception as e:
            logger.error(f"Ошибка отправки файла: {e}")
            await context.bot.send_message(
                chat_id=update.message.chat_id,
                text=f"Не удалось отправить файл {idx + 1}."
            )

    # Кнопка для удаления всех файлов
    delete_all_keyboard = InlineKeyboardMarkup([
        [InlineKeyboardButton("Удалить все файлы", callback_data="delete_all_files")]
    ])
    await context.bot.send_message(
        chat_id=update.message.chat_id,
        text="Выберите действие для всех файлов:",
        reply_markup=delete_all_keyboard
    )

async def show_files_callback(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Sends each uploaded file back to the user with an option to delete.
    This function is called from callback queries.
    """
    query = update.callback_query
    await query.answer()

    # Проверяем, инициализирован ли incident в user_data
    if 'incident' not in context.user_data or not context.user_data['incident']["files"]:
        await query.edit_message_text("Файлы не добавлены.")
        return

    # Отправляем каждое фото/видео пользователю с кнопками
    for idx, file in enumerate(context.user_data['incident']["files"]):
        keyboard = InlineKeyboardMarkup([
            [InlineKeyboardButton(f"Удалить этот файл", callback_data=f"delete_file_{idx}")]
        ])

        try:
            # Проверяем тип файла в словаре метаданных
            if isinstance(file, dict) and 'file_type' in file:
                file_id = file.get('file_id')
                if file['file_type'] == 'photo':
                    await context.bot.send_photo(
                        chat_id=query.message.chat_id,
                        photo=file_id,
                        reply_markup=keyboard
                    )
                elif file['file_type'] == 'video':
                    await context.bot.send_video(
                        chat_id=query.message.chat_id,
                        video=file_id,
                        reply_markup=keyboard
                    )
                else:
                    await context.bot.send_message(
                        chat_id=query.message.chat_id,
                        text=f"Неизвестный формат файла: {file_id}"
                    )
            # Запасной вариант - проверка по расширению файла
            elif isinstance(file, dict) and 'file_path' in file:
                file_id = file.get('file_id')
                if file['file_path'].endswith(('.jpg', '.png')):
                    await context.bot.send_photo(
                        chat_id=query.message.chat_id,
                        photo=file_id,
                        reply_markup=keyboard
                    )
                elif file['file_path'].endswith('.mp4'):
                    await context.bot.send_video(
                        chat_id=query.message.chat_id,
                        video=file_id,
                        reply_markup=keyboard
                    )
                else:
                    await context.bot.send_message(
                        chat_id=query.message.chat_id,
                        text=f"Неизвестный формат файла: {file_id}"
                    )
            # Обратная совместимость со старым форматом
            elif hasattr(file, 'file_id'):
                file_id = file.file_id
                logger.warning(f"Файл в старом формате: {file_id}")
                await context.bot.send_message(
                    chat_id=query.message.chat_id,
                    text=f"Неизвестный формат файла: {file_id}"
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

    # Проверяем, инициализирован ли incident в user_data
    if 'incident' not in context.user_data:
        await query.message.reply_text("Ошибка: данные инцидента не найдены.")
        return

    if query.data.startswith("delete_file_"):
        file_idx = int(query.data.split("_")[-1])

        if 0 <= file_idx < len(context.user_data['incident']["files"]):
            deleted_file = context.user_data['incident']["files"].pop(file_idx)
            logger.info(f"File deleted: {deleted_file.get('file_id')}")
            await query.message.reply_text(f"Файл {file_idx + 1} удалён.")
        else:
            await query.message.reply_text("Ошибка: файл не найден.")

    elif query.data == "delete_all_files":
        context.user_data['incident']["files"].clear()
        logger.info("All files deleted.")
        await query.message.reply_text("Все файлы удалены.")

    # Обновляем клавиатуру после удаления файлов
    reply_markup = generate_keyboard(context)
    await query.message.reply_text("Клавиатура обновлена", reply_markup=reply_markup)


async def finish_incident(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Directly sends the incident data to the backend without additional confirmation steps
    """
    logger.info(f"User {update.effective_user.username} used finish_incident.")

    # Проверяем, инициализирован ли incident в user_data
    if 'incident' not in context.user_data:
        await update.message.reply_text("⚠️ Данные инцидента не найдены. Пожалуйста, начните создание инцидента заново.")
        return

    # Проверяем, заполнены ли все поля
    if not context.user_data['incident'].get("title") or not context.user_data['incident'].get("description") or not context.user_data['incident'].get("files") or not context.user_data['incident'].get("address"):
        await update.message.reply_text("⚠️ Не все данные заполнены. Пожалуйста, убедитесь, что вы указали название, описание, добавили файлы и местоположение.")
        return

    # Показываем сообщение о начале отправки
    sending_message = await update.message.reply_text("⏳ Отправляем данные...")

    # Логика отправки данных
    try:
        zip_buffer = await finalize_incident(context.user_data['incident'])
        await send_incident_to_backend(zip_buffer, context.user_data['incident'], update)
        
        # Обновляем сообщение об успешной отправке
        await sending_message.edit_text("✅ Инцидент успешно отправлен!")
        logger.info("Incident successfully sent to the backend.")

        # Очистка данных
        context.user_data['incident']['files'].clear()
        context.user_data['incident']['title'] = None
        context.user_data['incident']['description'] = None
        context.user_data['incident']['address'] = None
        context.user_data['incident']["incidentType"] = None
        logger.info("Incident data cleared.")

        # Генерация минимальной клавиатуры
        reply_markup = generate_keyboard(context, minimal=True)

        # Отправляем новое сообщение с клавиатурой
        await update.message.reply_text("🚀 Хотите сообщить о новом инциденте?", reply_markup=reply_markup)

    except Exception as e:
        logger.error(f"Error while sending data: {e}")
        await sending_message.edit_text("⚠️ Ошибка при отправке данных. Пожалуйста, попробуйте еще раз.")
    
    
async def edit_data_request(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """
    Shows the current incident data and options to edit fields or send the data.
    """
    # Проверяем, инициализирован ли incident в user_data
    if 'incident' not in context.user_data:
        context.user_data['incident'] = {
            'files': [],
            'title': None,
            'description': None,
            'incidentType': None,
            'address': None
        }
    
    # Генерируем сообщение с текущими данными
    summary = generate_summary_message(context)

    # Кнопки для редактирования и подтверждения
    keyboard = [
        [InlineKeyboardButton("✏️ Название", callback_data="edit_title"), InlineKeyboardButton("✏️ Описание", callback_data="edit_description")],
        [InlineKeyboardButton("📍 Местоположение", callback_data="edit_location"), InlineKeyboardButton("🔍 Тип инцидента", callback_data="edit_type")],
        [InlineKeyboardButton("📁 Управление файлами", callback_data="show_files")]
    ]
    
    # Если все необходимые данные заполнены, добавляем кнопку отправки
    if context.user_data['incident'].get('title') and context.user_data['incident'].get('description') and context.user_data['incident'].get('files') and context.user_data['incident'].get('address'):
        keyboard.append([InlineKeyboardButton("✅ Отправить инцидент", callback_data="confirm_send")])
    
    reply_markup = InlineKeyboardMarkup(keyboard)

    await update.message.reply_text("📋 Данные вашего инцидента:\n\n" + summary, reply_markup=reply_markup)
    
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
    Sends the incident data to the backend from inline keyboard button.
    """
    query = update.callback_query
    await query.answer()

    # Проверяем, заполнены ли все поля
    if not incident_data["title"] or not incident_data["description"] or not incident_data.get("files") or not incident_data.get("address"):
        await query.edit_message_text("⚠️ Не все данные заполнены. Пожалуйста, убедитесь, что вы указали название, описание, добавили файлы и местоположение.")
        return

    await query.edit_message_text("⏳ Отправляем данные...")

    # Логика отправки данных
    try:
        zip_buffer = await finalize_incident(incident_data)
        await send_incident_to_backend(zip_buffer, incident_data, update)
        await query.edit_message_text("✅ Инцидент успешно отправлен!")
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
        await query.message.reply_text("🚀 Хотите сообщить о новом инциденте?", reply_markup=reply_markup)

    except Exception as e:
        logger.error(f"Error while sending data: {e}")
        await query.edit_message_text("⚠️ Ошибка при отправке данных. Пожалуйста, попробуйте еще раз.")

