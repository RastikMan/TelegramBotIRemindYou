package ua.rmergeee.javabot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.rmergeee.javabot.config.Config;
import ua.rmergeee.javabot.message.MessageRepository;

@Component
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private MessageRepository messageRepository;
    private final Config config;

    public Bot(Config config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String getMessage = update.getMessage().getText();
            long getChatId = update.getMessage().getChatId();

            switch (getMessage) {
                case "/start":
                    startCommandReceived(getChatId);
                    sendMessage(getChatId, "Для того щоб почати натисни на Додати Нагадування:");
                    break;
                default:
                    sendMessage(getChatId, getMessage);
                    break;
                case "Ти еблан?":
                    sendMessage(getChatId, "УЕБИЩЕ ТИ");
                    break;
                case "/addnotification":
                    sendMessage(getChatId, "Про що вам нагадати?");
                    break;
            }
        }
    }

    public void startCommandReceived(Long chatId) { // Метод для відправки повідомлення при настисненні кнопки старт
        String startMessage = "Вітаю! Я допоможу тобі не забути про важливе!";
        sendMessage(chatId, startMessage);
    }
    public void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException ignored) {

        }
    }


}
