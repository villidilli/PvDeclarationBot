package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kevdev.PvDeclarationBot.model.ChatModel;

public interface ChatRepo extends JpaRepository<ChatModel, Long> {
    ChatModel findByChatIdTelegram(Long chatIdTelegram);
}