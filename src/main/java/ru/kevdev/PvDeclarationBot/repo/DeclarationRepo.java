package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kevdev.PvDeclarationBot.model.Declaration;

import java.util.Optional;

@Repository
public interface DeclarationRepo extends JpaRepository<Declaration, String> {
}