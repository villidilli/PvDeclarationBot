package ru.kevdev.PvDeclarationBot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface BotService {
	SendMessage collectAnswer(Long chatId, String textToSend);
	
	SendMessage collectAnswer(Long chatId, String textToSend, InlineKeyboardMarkup keyboard);
	
	String makeGreeting(Chat chat);
	
	SendMessage getAnswer(Update update);

	SendDocument getAnswer(Update update);
}