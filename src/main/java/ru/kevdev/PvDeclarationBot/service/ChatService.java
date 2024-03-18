package ru.kevdev.PvDeclarationBot.service;

import ru.kevdev.PvDeclarationBot.model.ChatModel;
import ru.kevdev.PvDeclarationBot.model.User;

import java.util.Optional;

public interface ChatService {
	ChatModel saveChat(Long chatId, User actualUser);

	Optional<ChatModel> getChat(Long chatId);
}