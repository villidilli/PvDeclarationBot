package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kevdev.PvDeclarationBot.exception.NotFoundException;
import ru.kevdev.PvDeclarationBot.model.ChatModel;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.ChatRepo;
import ru.kevdev.PvDeclarationBot.utils.ChatMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
	private final ChatRepo chatRepo;

	@Override
	public ChatModel saveChat(Long chatIdTelegram, User actualUser) {
		return chatRepo.save(ChatMapper.toChat(chatIdTelegram, actualUser));
	}


	@Override
	public Optional<ChatModel> getChat(Long chatId) {
		return chatRepo.findById(chatId);
	}
}