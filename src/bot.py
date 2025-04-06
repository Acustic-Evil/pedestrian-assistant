from telegram.ext import ApplicationBuilder, CommandHandler, MessageHandler, ConversationHandler, filters, CallbackQueryHandler
from handlers.commands import SELECT_TYPE, confirm_send, delete_file, edit_data_request, edit_field, save_edited_field, save_location, set_incident_type, show_files, show_files_callback, start, start_incident, set_title, set_description, cancel, add_file, finish_incident, TITLE, DESCRIPTION
from utils.logger import logger
from dotenv import load_dotenv
import os

# Загрузка токена из .env файла
load_dotenv()
TOKEN = os.getenv("TELEGRAM_TOKEN")

if not TOKEN:
    logger.error("Telegram Token not found in .env.")
    exit("Add TELEGRAM_TOKEN into .env")

def main():
    logger.info("Bot initialization...")

    # Создание приложения
    application = ApplicationBuilder().token(TOKEN).build()

    # Обработчик для начала создания инцидента
    conversation_handler = ConversationHandler(
        entry_points=[
            CommandHandler("start_incident", start_incident),
            MessageHandler(filters.Regex(r"^🚀 Начать новый инцидент$"), start_incident)
        ],
        states={
            TITLE: [
                MessageHandler(filters.TEXT & ~filters.COMMAND & ~filters.Regex(r"^❌ Отменить$"), set_title),
                MessageHandler(filters.Regex(r"^❌ Отменить$"), cancel),
                CommandHandler("cancel", cancel)
            ],
            DESCRIPTION: [
                MessageHandler(filters.TEXT & ~filters.COMMAND & ~filters.Regex(r"^❌ Отменить$"), set_description),
                MessageHandler(filters.Regex(r"^❌ Отменить$"), cancel),
                CommandHandler("cancel", cancel)
            ],
            SELECT_TYPE: [
                MessageHandler(filters.TEXT & ~filters.COMMAND & ~filters.Regex(r"^❌ Отменить$"), set_incident_type),
                MessageHandler(filters.Regex(r"^❌ Отменить$"), cancel),
                CommandHandler("cancel", cancel)
            ],
        },
        fallbacks=[
            CommandHandler("cancel", cancel),
            MessageHandler(filters.Regex(r"^❌ Отменить$"), cancel)
        ]
    )

    application.add_handler(conversation_handler)

    # Основные команды
    application.add_handler(CommandHandler("start", start))
    
    # Обработчики для кнопок и команд
    application.add_handler(MessageHandler(filters.Regex(r"^✅ Отправить инцидент$"), finish_incident))
    application.add_handler(CommandHandler("finish", finish_incident))
    
    application.add_handler(MessageHandler(filters.Regex(r"^❌ Отменить$"), cancel))
    application.add_handler(CommandHandler("cancel", cancel))
    
    application.add_handler(MessageHandler(filters.Regex(r"^✏️ Редактировать$"), edit_data_request))
    application.add_handler(CommandHandler("edit", edit_data_request))
    
    application.add_handler(MessageHandler(filters.Regex(r"^📁 Файлы$"), show_files))
    
    application.add_handler(CallbackQueryHandler(edit_field, pattern="edit_.*"))
    application.add_handler(CallbackQueryHandler(show_files_callback, pattern="show_files"))
    application.add_handler(CallbackQueryHandler(confirm_send, pattern="confirm_send"))
    application.add_handler(CallbackQueryHandler(delete_file, pattern="delete_file_.*"))
    application.add_handler(CallbackQueryHandler(delete_file, pattern="delete_all_files"))
    
    application.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, save_edited_field))
    application.add_handler(MessageHandler(filters.PHOTO | filters.VIDEO, add_file))
    application.add_handler(MessageHandler(filters.LOCATION, save_location))

    # Запуск бота
    logger.info("Bot started...")
    application.run_polling()

if __name__ == "__main__":
    main()
