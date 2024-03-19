package ru.kevdev.PvDeclarationBot.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.model.ChatModel;
import ru.kevdev.PvDeclarationBot.utils.ButtonCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.kevdev.PvDeclarationBot.utils.ButtonCommand.*;

@Service
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {
	@Value("${bot-name}")
	private String botName;
	@Value("${bot-token}")
	private String botToken;
	private UserService userService;
	private ChatService chatService;

	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@EventListener({ContextRefreshedEvent.class})
	public void init() {
		try {
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramBotsApi.registerBot(this);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

//	@Override
//	public void onUpdateReceived(Update update) {
//			getAnswer(update);
//	}
//	@SneakyThrows
//	@Override
////	public void onUpdateReceived(Update update) {
//		Long chatId = update.getMessage().getChatId();
//		if (update.hasMessage() && update.getMessage().hasText()) {
//			String input = getKeyboardInputValue(update);
//			if (input.equalsIgnoreCase("/start")) {
//				if (chatService.getChat(chatId).isEmpty()) {
//					execute(collectAnswer(chatId, "Для авторизации введите рабочий email -->\n "));
//				} else {
//					InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
//							.keyboardRow(List.of(getButton("Декларации соответствия", GET_DECLARATION.name())))
//							.keyboardRow(List.of(getButton("Качественные удостоверения", GET_QUALITY.name())))
//							.keyboardRow(List.of(getButton("Макеты этикеток", GET_LABEL_LAYOUTS.name())))
//							.build();
//					execute(collectAnswer(chatId,
//								"Вы уже авторизованы.\n Какой документ хотите загрузить ?", keyboard));
//				}
//			}
//		} else if (update.hasCallbackQuery()) {
//
//		} else {
//			execute(collectAnswer(update.getMessage().getChatId(),
//					"Упс... Что-то пошло не так"));
//		}
//	}

	@SneakyThrows
	@Override
	public void onUpdateReceived(Update update) {
		//пришло сообщение
		System.out.println(update);

	}

	private InlineKeyboardMarkup getKeyboard1R1B(List<InlineKeyboardButton> buttons) {
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> buttonsRows = new ArrayList<>();
		for (int i = 0; i < buttons.size() - 1 ; i++) {
			buttonsRows.add(List.of(buttons.get(i)));
		}
		keyboard.setKeyboard(buttonsRows);
		return keyboard;
	}

	@SneakyThrows
	public void getAnswer(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			getUserInputAnswer(update);
		} else if (update.hasCallbackQuery()) {
			getCallbackQueryAnswer(update);
		} else {
			execute(collectAnswer(update.getMessage().getChatId(),
					"Упс... Что-то пошло не так"));
		}
	}

	private String readKeyboardInput(Update update) {
		return update.getMessage().getText();
	}

	@SneakyThrows
	private void getUserInputAnswer(Update update) {
		String messageText = update.getMessage().getText();
		Long chatId = update.getMessage().getChatId();
		if (messageText.equals("/start")) {
			execute(collectAnswer(chatId, makeGreeting(update.getMessage().getChat())));
			return;
		}
		Optional<User> existedUser = userService.getUser(messageText);
		Optional<ChatModel> existedChat = chatService.getChat(chatId);
		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(getButton("Декларации соответствия", GET_DECLARATION.name())))
				.keyboardRow(List.of(getButton("Качественные удостоверения", GET_QUALITY.name())))
				.build();
		if (existedChat.isPresent()) {
			execute(collectAnswer(chatId, "Хммм...Вы уже авторизованы...\nПоработаем ?", keyboard));
			return;
		} else if (existedUser.isEmpty()) {
			execute(collectAnswer(chatId, "Пользователь с email: " + messageText + " не найден !\n\n" +
													"Для получения доступа обращаться ekuznecov@ecln.ru"));
			return;
		} else if (existedChat.isEmpty()){
			chatService.saveChat(chatId, existedUser.get());
			execute(collectAnswer(chatId, "Успешная авторизация! Поехали --->\n", keyboard));
			return;
		}
		execute(collectAnswer(chatId, "Упс...Что-то накодил не так..."));
	}

	//todo
	@SneakyThrows
	private void getCallbackQueryAnswer(Update update) {
		Long chatId = update.getCallbackQuery().getMessage().getChatId();
		ButtonCommand command = ButtonCommand.valueOf(update.getCallbackQuery().getData());
		switch (command) {
			case GET_DECLARATION:
				execute(collectAnswer(chatId, "Введите штрихкод товара"));
				return;
			case GET_QUALITY:  execute(collectAnswer(chatId, "Отправляю качественное"));
		}
	}

	private SendDocument getDeclaration(Long chatId) {
		return SendDocument.builder()
				.document(new InputFile(new File("C:\\Users\\Usserss\\Documents\\temp\\ОиДТ.xlsx")))
				.chatId(chatId)
				.build();
	}

	private InlineKeyboardButton getButton(String textOnButton, String textToServer) {
		return InlineKeyboardButton.builder()
					.text(textOnButton)
					.callbackData(textToServer)
					.build();
	}

	private SendMessage collectAnswer(Long chatId, String textToSend) {
		return SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.build();
	}

	private SendMessage collectAnswer(Long chatId, String textToSend, InlineKeyboardMarkup keyboard) {
		return SendMessage.builder()
				.chatId(chatId)
				.text(textToSend)
				.replyMarkup(keyboard)
				.build();
	}

	private String makeGreeting(Chat chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("Доброго времени суток, ");
		sb.append(chat.getFirstName() + " ");
		sb.append(chat.getLastName() + " !\n\n");
		sb.append("Для получения информации необходимо авторизоваться\n");
		sb.append("Введите ваш рабочий EMAIL --->");
		return sb.toString();
	}
}