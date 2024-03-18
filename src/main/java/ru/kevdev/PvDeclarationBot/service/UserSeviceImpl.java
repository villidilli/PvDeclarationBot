package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.UserRepo;

import java.util.List;

@Service
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
@RequiredArgsConstructor
public class UserSeviceImpl implements UserService {
	private final UserRepo userRepo;
	
	@Override
	public User getUser(String email) {
		List<User> actualListUsers = userRepo.findAllByEmail(email);
		return actualListUsers.get(0);
	}
}