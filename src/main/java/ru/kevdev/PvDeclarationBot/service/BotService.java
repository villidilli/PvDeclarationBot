package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.kevdev.PvDeclarationBot.model.Declaration;
import ru.kevdev.PvDeclarationBot.model.Product;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.DeclarationRepo;
import ru.kevdev.PvDeclarationBot.repo.ProductRepo;
import ru.kevdev.PvDeclarationBot.utils.ButtonCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.kevdev.PvDeclarationBot.utils.ButtonCommand.*;

@Service
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {
	private String botName;
	private String botToken;
	private UserService userService;
	private ChatService chatService;
	private DeclarationRepo declarationRepo;
	private ProductRepo productRepo;
	private Long chatId;
	private String lastMsg = "";
	private String curMsg = "";
	private InlineKeyboardMarkup kb;

	@Autowired
	public BotService(@Value("${bot-name}") String botName, @Value("${bot-token}") String botToken,
					  UserService userService, ChatService chatService,
					  DeclarationRepo declarationRepo, ProductRepo productRepo) {
		this.botName = botName;
		this.botToken = botToken;
		this.userService = userService;
		this.chatService = chatService;
		this.productRepo = productRepo;
		this.declarationRepo = declarationRepo;
	}

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

	@SneakyThrows
	@Override
	public void onUpdateReceived(Update update) {
		// есть пришло сообщение через поле ввода
		if (update.hasMessage() && update.getMessage().hasText()) {
			chatId = update.getMessage().getChatId();
			curMsg = update.getMessage().getText();

			if (curMsg.equalsIgnoreCase("/" + START.name())) { // обработка стартовой команды
				doStart();
			} else if (lastMsg != null && lastMsg.equals(AUTHORIZATION.name())) { //проверка на ввод пользователем почты
				doAuthorization();
			} else if (lastMsg != null && lastMsg.equals(BY_ERP_CODE.name())) {
				String codeWithoutZero = curMsg.replace("0", "");
				execute(collectAnswer(chatId, "Загружаю файл...\nКак будет готов, сразу пришлю")); //TODO
				getDeclarationByErpCode(codeWithoutZero, chatId);
				execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
			} else { //обратка бессмысленного ввода в поле, например, когда ожидается ввод команды, а приходит сообщение
				execute(collectAnswer(chatId, "ОШИБКА -> Не знаю, что с этим делать..."));
			}
		}
		//если пришло команда через кнопку
		if (update.hasCallbackQuery()) {
			chatId = update.getCallbackQuery().getMessage().getChatId();
			curMsg = update.getCallbackQuery().getData();
			switch (ButtonCommand.valueOf(curMsg)) {
				case AUTHORIZATION:
						execute(collectAnswer(chatId, "Введите в текстовое поле ваш рабочий email"));
					lastMsg = curMsg;
					break;
				case GET_DECLARATION:
					kb = InlineKeyboardMarkup.builder()
							.keyboardRow(List.of(getButton("по КОДу ERP", BY_ERP_CODE.name())))
							.keyboardRow(List.of(getButton("по ШТРИХКОДУ", BY_BARCODE.name())))
							.build();
					execute(collectAnswer(chatId, "Варианты загрузки -->", kb));
					lastMsg = curMsg;
					break;
				case BY_ERP_CODE:
					execute(collectAnswer(chatId, "Введите код ЕРП"));
					lastMsg = curMsg;
					break;
			}
		}
	}

	@SneakyThrows //TODO
	private void getDeclarationByErpCode(String code, Long chatId) {
		Optional<Product> existedProduct = productRepo.findById(Long.valueOf(code));
		if (existedProduct.isPresent()) {
			Product prod = existedProduct.get();
			Declaration existedDeclaration = existedProduct.get().getDeclaration();
			String pathToFile = existedProduct.get().getDeclaration().getPathToFile();
			File file = new File(pathToFile);
			if (file.exists() && !file.isDirectory()) {
				SendDocument documentToSend = SendDocument.builder()
						.chatId(chatId)
						.document(new InputFile(new File(pathToFile)))
						.build();
				execute(documentToSend);
			} else {
				execute(collectAnswer(chatId, "ОШИБКА --> Файл не найден..."));
			}
		} else {
			execute(collectAnswer(chatId, "ОШИБКА --> Товар не найден..."));
		}
	}

	private InlineKeyboardMarkup getDocumentTypesKeyboard() {
		return InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(getButton("Декларация соответствия", GET_DECLARATION.name())))
				.keyboardRow(List.of(getButton("Качественное удостоверение", GET_QUALITY.name())))
				.keyboardRow(List.of(getButton("Макет этикетки", GET_LABEL_MOCKUP.name())))
				.build();
	}

	@SneakyThrows
	private void doAuthorization() {
		Optional<User> user = userService.getUser(curMsg); //проверка в БД на наличие пользователя
		if (user.isPresent()) {
			chatService.saveChat(chatId, user.get());
			kb = InlineKeyboardMarkup.builder()
					.keyboardRow(List.of(getButton("ДС", GET_DECLARATION.name()),
							getButton("КУ", GET_QUALITY.name())))
					.build();
			execute(collectAnswer(chatId, "Отлично! Поехали!\n" + "Что будем загружать?", kb));
			lastMsg = curMsg;
		} else { // если пользователя нет в БД
			execute(collectAnswer(chatId, "По-моему я вас не знаю...\nДавайте попробуем ещё раз?\nлибо напишите на ekuznecov@ecln.ru"));
		}
	}

	@SneakyThrows
	private void doStart() {
		kb = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(getButton("Авторизоваться", AUTHORIZATION.name())))
				.build();
		execute(collectAnswer(chatId,
				"Вас приветствует Бот-Документ!\n" + "Давайте познакомимся ?",
				kb));
		lastMsg = curMsg;
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

	private InlineKeyboardMarkup getKeyboard1R1B(List<InlineKeyboardButton> buttons) {
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> buttonsRows = new ArrayList<>();
		for (int i = 0; i < buttons.size() - 1 ; i++) {
			buttonsRows.add(List.of(buttons.get(i)));
		}
		keyboard.setKeyboard(buttonsRows);
		return keyboard;
	}
}