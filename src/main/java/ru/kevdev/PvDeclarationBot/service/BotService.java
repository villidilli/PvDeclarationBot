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
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.kevdev.PvDeclarationBot.model.Product;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.DeclarationRepo;
import ru.kevdev.PvDeclarationBot.repo.ProductRepo;
import ru.kevdev.PvDeclarationBot.utils.ButtonCommand;
import ru.kevdev.PvDeclarationBot.utils.Constant;

import java.io.File;
import java.util.*;

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
		if (update.hasMessage() && update.getMessage().hasText()) {
			userInputProcessing(update);
		}
		if (update.hasCallbackQuery()) {
			callBackQueryProcessing(update);
		}
	}

	@SneakyThrows
	// есть пришло сообщение через поле ввода
	private void userInputProcessing(Update update) {
		chatId = update.getMessage().getChatId();
		curMsg = update.getMessage().getText();

		if (curMsg.equalsIgnoreCase("/" + START.name())) { // обработка стартовой команды
			doStart();
		} else if (lastMsg != null && lastMsg.equals(AUTHORIZATION.name())) { //проверка на ввод пользователем почты
			doAuthorization();
		} else if (lastMsg != null && lastMsg.equals(BY_ERP_CODE.name())) {
			execute(collectAnswer(chatId, "Загружаю файл...\nКак будет готов, сразу пришлю")); //TODO
			getDeclarationByErpCode(curMsg, chatId);
			execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
		} else { //обратка бессмысленного ввода в поле, например, когда ожидается ввод команды, а приходит сообщение
			execute(collectAnswer(chatId, "ОШИБКА -> Не знаю, что с этим делать..."));
		}
	}

	@SneakyThrows
	//если пришло команда через кнопку
	private void callBackQueryProcessing(Update update) {
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
			case GET_QUALITY:
				execute(collectAnswer(chatId, "Извиняюсь, функционал в стадии разработки")); //TODO
				break;
			case GET_LABEL_MOCKUP:
				execute(collectAnswer(chatId, "Извиняюсь, функционал в стадии разработки")); //TODO
				break;
			case BY_BARCODE:
				execute(collectAnswer(chatId, "Извиняюсь, функционал в стадии разработки")); //TODO
				break;
		}
	}

	@SneakyThrows //TODO
	private void getDeclarationByErpCode(String code, Long chatId) {
		if (!isStringNumeric(code)) { //проверка что сообщение не содержит букв
			execute(collectAnswer(chatId, "ОШИБКА --> Не является числом"));
			execute(collectAnswer(chatId, "повторите попытку -->"));
		}
		Long codeWithoutZero = cutFrontZero(code); // обрезаем впереди стоящие нули
 		Optional<Product> existedProduct = productRepo.findById(codeWithoutZero);
		if (existedProduct.isPresent()) { // если товар найден
			String pathToFile = Constant.pathDirDeclarations + existedProduct.get().getDeclaration().getFileName(); //получаем декларацию и путь
			File file = new File(pathToFile);
			if (file.exists() && !file.isDirectory()) { // проверяем доступен ли файл
				SendDocument documentToSend = SendDocument.builder()
						.chatId(chatId)
						.document(new InputFile(file))
						.build();
				execute(documentToSend);
			} else {
				execute(collectAnswer(chatId, "ОШИБКА --> Файл не найден..."));
			}
		} else {
			execute(collectAnswer(chatId, "ОШИБКА --> Товар не найден..."));
		}
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
			execute(collectAnswer(chatId, "Успешная авторизация!"));
			execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
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

		private boolean isStringNumeric(String string) {
		try {
			Long.parseLong(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private Long cutFrontZero(String code) { //Обрезает все нули спереди до первого "не ноля"
		List<String> charList = new ArrayList<>(Arrays.asList(code.split("")));

		for (Iterator<String> it = charList.iterator(); it.hasNext();) {
			if (it.next().equals("0")) {
				it.remove();
			} else {
				break;
			}
		}
		return Long.parseLong(String.join("", charList));
	}

	private InlineKeyboardMarkup getDocumentTypesKeyboard() {
		return InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(getButton("Декларация соответствия", GET_DECLARATION.name())))
				.keyboardRow(List.of(getButton("Качественное удостоверение", GET_QUALITY.name())))
				.keyboardRow(List.of(getButton("Макет этикетки", GET_LABEL_MOCKUP.name())))
				.build();
	}
}