package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kevdev.PvDeclarationBot.model.Declaration;

import java.util.Optional;

public interface DeclarationRepo extends JpaRepository<Declaration, String> {
}