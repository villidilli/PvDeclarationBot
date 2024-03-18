package ru.kevdev.PvDeclarationBot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class BotServiceImpl implements BotService {
	
	@Override
	public SendMessage collectAnswer(Long chatId, String textToSend) {
		SendMessage answer = SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.build();
		return answer;
	}
	
	@Override
	public String makeGreeting(Chat chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("Доброго времени суток, ");
		sb.append(chat.getFirstName() + " ");
		sb.append(chat.getLastName() + " !\n\n");
		sb.append("Для получения информации необходимо авторизоваться\n");
		sb.append("Введите ваш рабочий EMAIL --->");
		return sb.toString();
	}
	
	
	@Override
	public SendMessage getAnswer(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			switch (messageText) {
				case "/start":
					return collectAnswer(update.getMessage().getChatId(), makeGreeting(update.getMessage().getChat()));
			}
		}
		return null;
	}
}