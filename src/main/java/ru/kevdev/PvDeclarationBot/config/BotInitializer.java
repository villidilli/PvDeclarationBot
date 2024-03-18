package ru.kevdev.PvDeclarationBot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.kevdev.PvDeclarationBot.model.BotController;

@Component
@RequiredArgsConstructor
public class BotInitializer {
	private final BotController botController;
	
//	@Autowired
//	public BotInitializer(BotController botController) {
//		this.botController = botController;
//	}
	
	@EventListener({ContextRefreshedEvent.class})
	public void init() {
		try {
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramBotsApi.registerBot(botController);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
}