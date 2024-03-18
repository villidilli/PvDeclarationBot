package ru.kevdev.PvDeclarationBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {
	private final String botName;
	private final String botToken;
	
	public Bot(@Value("${bot-name}") String botName,
	           @Value("${bot-token}") String botToken) {
		this.botName = botName;
		this.botToken = botToken;
	}
	
	@Override
	public String getBotUsername() {
		return botName;
	}
	
	@Override
	public String getBotToken() {
		return botToken;
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		if (!update.hasMessage() && !update.getMessage().hasText()) return;
		String message = update.getMessage().getText();
		switch (message) {
			case "/start":
				sendAnswer(update.getMessage().getChatId(), makeGreeting(update.getMessage().getChat()));
		}
	}
	
	private void sendAnswer(Long chatId, String textToSend) {
		SendMessage answer = SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.build();
		try {
			execute(answer);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String makeGreeting(Chat chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("Доброго времени суток, ");
		sb.append(chat.getFirstName() + " ");
		sb.append(chat.getLastName() + " !\n\n");
		sb.append("Для получения информации необходимо авторизоваться\n");
		sb.append("Введите ваш рабочий EMAIL --->");
		return sb.toString();
	}
}