package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kevdev.PvDeclarationBot.model.LabelMockup;

@Repository
public interface LabelMockupsRepo extends JpaRepository<LabelMockup, Long> {
}