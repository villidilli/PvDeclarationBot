package ru.kevdev.PvDeclarationBot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotService {
	SendMessage collectAnswer(Long chatId, String textToSend);
	String makeGreeting(Chat chat);
	
	SendMessage getAnswer(Update update);
}