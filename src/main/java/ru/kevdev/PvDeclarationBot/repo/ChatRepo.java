package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kevdev.PvDeclarationBot.model.ChatModel;

@Repository
public interface ChatRepo extends JpaRepository<ChatModel, Long> {
    ChatModel findByChatIdTelegram(Long chatIdTelegram);
}