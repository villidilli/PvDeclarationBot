package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kevdev.PvDeclarationBot.exception.NotFoundException;
import ru.kevdev.PvDeclarationBot.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {
	
	private final UserService userService;
	private final ChatService chatService;
	
	@Override
	public SendMessage getAnswer(Update update) {
		
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			Long chatId = update.getMessage().getChatId();
			
			if (messageText.equals("/start")) {
				return collectAnswer(chatId, makeGreeting(update.getMessage().getChat()));
			}
			
			try {
				User actualUser = userService.getUser(messageText);
				chatService.saveChat(chatId, actualUser);
				//TODO кнопки действий. Переделать кратко с Телеги доки
				InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
						.keyboardRow(List.of(getButton("Загрузить ДС ЕАЭС", "GET_DECLARATION")))
						.build();
				return collectAnswer(chatId, "Вы успешно авторизовались !", keyboard);
				
				
			} catch (NotFoundException e) {
				return collectAnswer(chatId, "Пользователь с таким EMAIL не найден.\n" +
														"Повторите ввод, либо напишите на почту ekuznecov@ecln.ru\n\n" +
														"Введите ваш рабочий EMAIL --->");
			}
		}
		
		return collectAnswer(update.getMessage().getChatId(),
				"Invalid input. Please try again /start"); //TODO подумать
	}
	
	private InlineKeyboardButton getButton(String textOnButton, String textToServer) {
		return InlineKeyboardButton.builder()
					.text(textOnButton)
					.callbackData(textToServer)
					.build();
	}
	
	@Override
	public SendMessage collectAnswer(Long chatId, String textToSend) {
		SendMessage answer = SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.build();
		return answer;
	}
	
	@Override
	public SendMessage collectAnswer(Long chatId, String textToSend, InlineKeyboardMarkup keyboard) {
		SendMessage answer = SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.replyMarkup(keyboard)
				.build();
		return answer;
	}
	
	@Override
	public String makeGreeting(Chat chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("Доброго времени суток, ");
		sb.append(chat.getFirstName() + " ");
		sb.append(chat.getLastName() + " !\n\n");
		sb.append("Для получения информации необходимо авторизоваться\n");
		sb.append("Введите ваш рабочий EMAIL --->");
		return sb.toString();
	}
}