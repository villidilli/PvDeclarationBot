package ru.kevdev.PvDeclarationBot.service;

import ru.kevdev.PvDeclarationBot.model.User;

public interface ChatService {
	void saveChat(Long chatId, User actualUser);
}