package ua.rmergeee.javabot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.rmergeee.javabot.config.Config;
import ua.rmergeee.javabot.message.MessageEntity;
import ua.rmergeee.javabot.message.MessageRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Bot extends TelegramLongPollingBot {

    private final MessageRepository messageRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Config config;

    public Bot(Config config, MessageRepository messageRepository) {
        this.config = config;
        this.messageRepository = messageRepository;
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

        List<MessageEntity> reminders = messageRepository.findAll();
        for (MessageEntity reminder : reminders) {
            scheduleReminderTask(reminder);
        }

        if(update.hasMessage() && update.getMessage().hasText()) {
            String getMessage = update.getMessage().getText();
            long getChatId = update.getMessage().getChatId();

            switch (getMessage) {
                case "/start":
                    startCommandReceived(getChatId);
                    sendMessage(getChatId, "Для того щоб почати напиши про що тобі нагадати. У такому форматі:");
                    sendMessage(getChatId, "ДД.MM.РРРР ГГ:ХХ Твоє нагадування");
                    break;
                default:
                    try {
                        String textMessage = getTextMessage(getMessage);
                        LocalDateTime dateTime = getDateTime(getMessage);

                        sendMessage(getChatId, "Ваше нагадування: " + textMessage);
                        sendMessage(getChatId, "Час нагадування: " + dateTime);

                        // Збереження в базі даних
                        saveReminder(getChatId, textMessage, dateTime);
                    } catch (Exception e) {
                        sendMessage(getChatId, "Ви вказали щось не так, або розробник зробив помилку, спробуйте ще раз! Приклад можно дізнатись за командою /help");
                    }


                    break;
                case "/help":
                    sendMessage(getChatId, "Напиши нагадування у такому форматі:");
                    sendMessage(getChatId, "ДД.MM.РРРР ГГ:ХХ Твоє нагадування");
                    break;
            }
        }
    }

    public void startCommandReceived(Long chatId) {
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
    private String getTextMessage(String message) {
        return message.substring("dd.MM.yyyy HH:mm".length()).trim();
    }
    private LocalDateTime getDateTime(String message) {
        String[] parts = message.split(" ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        try {
            String dateString = parts[0] + " " + parts[1];
            return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            return null;
        }
    }
    private void saveReminder(Long chatId, String textMessage, LocalDateTime dateTime) {
        MessageEntity reminderEntity = new MessageEntity();
        reminderEntity.setMessage(textMessage);
        reminderEntity.setDateTime(dateTime);
        reminderEntity.setChatId(chatId);

        messageRepository.save(reminderEntity);
        scheduleReminderTask(reminderEntity);
    }

    private void scheduleReminderTask(MessageEntity messageEntity) {
        long initialDelay = Duration.between(LocalDateTime.now(), messageEntity.getDateTime()).toMillis();
        scheduler.schedule(() -> {
            sendMessage(messageEntity.getChatId(), "Нагадую! ");
            sendMessage(messageEntity.getChatId(), "Ваше нагадування: " + messageEntity.getMessage());
            messageRepository.delete(messageEntity);
        }, initialDelay, TimeUnit.MILLISECONDS);
    }
}
