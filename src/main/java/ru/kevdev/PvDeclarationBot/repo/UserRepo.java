package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kevdev.PvDeclarationBot.model.User;

import java.util.List;

public interface UserRepo extends JpaRepository<User, Long> {
	List<User> findAllByEmail(String email);
}