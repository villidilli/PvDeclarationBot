package ru.kevdev.PvDeclarationBot.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kevdev.PvDeclarationBot.service.BotService;

@Component
public class BotController extends TelegramLongPollingBot {
	private final String botName;
	private final String botToken;
	private final BotService botService;
	
	public BotController(@Value("${bot-name}") String botName,
	                     @Value("${bot-token}") String botToken,
	                     BotService botService) {
		this.botName = botName;
		this.botToken = botToken;
		this.botService = botService;
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
		try {
			execute(botService.getAnswer(update));
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
}