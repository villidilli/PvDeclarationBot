package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kevdev.PvDeclarationBot.model.Chat;

public interface ChatRepo extends JpaRepository<Chat, Long> {
}