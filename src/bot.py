from telegram.ext import ApplicationBuilder, CommandHandler, MessageHandler, ConversationHandler, filters, CallbackQueryHandler
from handlers.commands import confirm_send, delete_file, edit_data_request, edit_field, save_edited_field, save_location, start, start_incident, set_title, set_description, cancel, add_file, finish_incident, TITLE, DESCRIPTION
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

    # Conversation handler for incident creation
    conv_handler = ConversationHandler(
        entry_points=[CommandHandler("start_incident", start_incident)],
        states={
            TITLE: [MessageHandler(filters.TEXT & ~filters.COMMAND, set_title)],
            DESCRIPTION: [MessageHandler(filters.TEXT & ~filters.COMMAND, set_description)],
        },
        fallbacks=[CommandHandler("cancel", cancel)],
    )

    # Регистрация обработчиков
    application.add_handler(conv_handler)
    
    application.add_handler(CommandHandler("start", start))
    application.add_handler(CommandHandler("finish", finish_incident))
    application.add_handler(CommandHandler("cancel", cancel))
    application.add_handler(CommandHandler("edit", edit_data_request))
    application.add_handler(CallbackQueryHandler(edit_field, pattern="edit_.*"))
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
