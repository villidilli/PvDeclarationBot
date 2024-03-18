package ru.kevdev.PvDeclarationBot.utils;

import ru.kevdev.PvDeclarationBot.model.ChatModel;
import ru.kevdev.PvDeclarationBot.model.User;

public class ChatMapper {
	
	public static ChatModel toChat(Long chatId, User user) {
		ChatModel newChat = new ChatModel();
		newChat.setChatIdTelegram(chatId);
		newChat.setUser(user);
		newChat.setVerified(true);
		return newChat;
	}
}