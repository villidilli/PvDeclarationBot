package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.kevdev.PvDeclarationBot.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
//	List<User> findAllByEmail(String email)

	Optional<User> findByEmail(String email);
}