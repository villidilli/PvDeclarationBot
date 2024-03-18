package ru.kevdev.PvDeclarationBot.service;

import ru.kevdev.PvDeclarationBot.model.User;

import java.util.Optional;

public interface UserService {
	Optional<User> getUser(String email);
}