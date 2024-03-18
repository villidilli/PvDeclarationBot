package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.kevdev.PvDeclarationBot.exception.NotFoundException;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.UserRepo;

import java.util.Optional;

@Service
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepo userRepo;
	
	@Override
	public Optional<User> getUser(String email) {
		return userRepo.findByEmail(email);
	}
}