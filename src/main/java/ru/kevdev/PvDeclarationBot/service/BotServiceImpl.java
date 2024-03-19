package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kevdev.PvDeclarationBot.exception.NotFoundException;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.model.ChatModel;
import ru.kevdev.PvDeclarationBot.utils.ButtonCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static ru.kevdev.PvDeclarationBot.utils.ButtonCommand.GET_DECLARATION;
import static ru.kevdev.PvDeclarationBot.utils.ButtonCommand.GET_QUALITY;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

	private final UserService userService;
	private final ChatService chatService;

	@Override
	public SendMessage getAnswer(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			return getUserInputAnswer(update);
		} else if (update.hasCallbackQuery()) {
			return getCallbackQueryAnswer(update);
		}
		return collectAnswer(update.getMessage().getChatId(),
				"Упс... Что-то пошло не так");
	}

	private SendMessage getUserInputAnswer(Update update) {
		String messageText = update.getMessage().getText();
		Long chatId = update.getMessage().getChatId();
		if (messageText.equals("/start")) {
			return collectAnswer(chatId, makeGreeting(update.getMessage().getChat()));
		}
		Optional<User> existedUser = userService.getUser(messageText);
		Optional<ChatModel> existedChat = chatService.getChat(chatId);
		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(getButton("Декларации соответствия", GET_DECLARATION.name())))
				.keyboardRow(List.of(getButton("Качественные удостоверения", GET_QUALITY.name())))
				.build();
		if (existedChat.isPresent()) {
			return collectAnswer(chatId, "Хммм...Вы уже авторизованы...\nПоработаем ?", keyboard);
		} else if (existedUser.isEmpty()) {
			return collectAnswer(chatId, "Пользователь с email: " + messageText + " не найден !\n\n" +
													"Для получения доступа обращаться ekuznecov@ecln.ru");
		} else if (existedChat.isEmpty()){
			chatService.saveChat(chatId, existedUser.get());
			return collectAnswer(chatId, "Успешная авторизация! Поехали --->\n", keyboard);
		}
		return collectAnswer(chatId, "Упс...Что-то накодил не так...");
	}

	//todo
	private SendMessage getCallbackQueryAnswer(Update update) {
		Long chatId = update.getCallbackQuery().getMessage().getChatId();
		ButtonCommand command = ButtonCommand.valueOf(update.getCallbackQuery().getData());
		switch (command) {
			case GET_DECLARATION: return getDeclaration(update); // TODO ???
//			case GET_QUALITY: return collectAnswer(chatId, "Отправляю качественное");
			//default: return collectAnswer(chatId, "Неизвестный тип документа"); //TODO
		}
		return null;
	}

//	private SendDocument getDeclaration(Update update) {
//		return SendDocument.builder()
//				.document(new InputFile(new File(URI.create("C:\\Users\\Usserss\\YandexDisk\\Документы\\Прочее\\!!! Декл.соответствия\\ПВ\\Aceites Albert.Масло оливковое EV и PURE (02.2023).pdf"))))
//				.chatId(update.getMessage().getChatId())
//				.build();
//	}

	private InlineKeyboardButton getButton(String textOnButton, String textToServer) {
		return InlineKeyboardButton.builder()
					.text(textOnButton)
					.callbackData(textToServer)
					.build();
	}
	
	@Override
	public SendMessage collectAnswer(Long chatId, String textToSend) {
		return SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.build();
	}
	
	@Override
	public SendMessage collectAnswer(Long chatId, String textToSend, InlineKeyboardMarkup keyboard) {
		return SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.replyMarkup(keyboard)
				.build();
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