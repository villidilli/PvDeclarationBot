package ru.kevdev.PvDeclarationBot.service;

import ru.kevdev.PvDeclarationBot.model.User;

public interface UserService {
	User getUser(String email);
}