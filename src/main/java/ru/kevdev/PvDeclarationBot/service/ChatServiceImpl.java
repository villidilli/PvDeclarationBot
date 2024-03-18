package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.ChatRepo;
import ru.kevdev.PvDeclarationBot.utils.ChatMapper;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
	private final ChatRepo chatRepo;
	@Override
	public void saveChat(Long chatId, User actualUser) {
		chatRepo.save(ChatMapper.toChat(chatId, actualUser));
	}
}