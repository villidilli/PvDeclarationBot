package ru.kevdev.PvDeclarationBot.utils;

import ru.kevdev.PvDeclarationBot.model.Chat;
import ru.kevdev.PvDeclarationBot.model.User;

public class ChatMapper {
	
	public static Chat toChat(Long chatId, User user) {
		Chat newChat = new Chat();
		newChat.setChatIdTelegram(chatId);
		newChat.setUser(user);
		newChat.setVerified(true);
		return newChat;
	}
}