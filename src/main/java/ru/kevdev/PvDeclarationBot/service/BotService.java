package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
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
import ru.kevdev.PvDeclarationBot.model.Declaration;
import ru.kevdev.PvDeclarationBot.model.Product;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.ProductRepo;
import java.io.File;
import java.util.*;

import static ru.kevdev.PvDeclarationBot.utils.Constant.*;


@Service
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {
	@Value("${bot-name}")
	private String botName;
	@Value("${bot-token}")
	private String botToken;
	private final UserService userService;
	private final ChatService chatService;
	private final ProductRepo productRepo;
	private Long chatId;
	private String lastInput;
	private String curInput;
	private String lastCbq;
	private String curCbq;
	private InlineKeyboardMarkup kb;

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
		curInput = update.getMessage().getText();

		if (curInput.equalsIgnoreCase(START)) { // обработка стартовой команды
			doStart();
			return;
		}
		if (lastCbq != null && lastCbq.equals(AUTHORIZATION)) { //если последняя команда авторизация, значит введена почта
			doAuthorization(curInput);
			return;
		}
		if (lastCbq != null && lastCbq.equals(GET_BY_ERP_CODE)) {
			execute(collectAnswer(chatId, "Загружаю файл...\nКак будет готов, сразу пришлю"));
			getDeclarationByErpCode(curInput, chatId);
			return;
		}
		if (lastCbq != null && lastCbq.equals(GET_BY_BARCODE)) { //если посл.команда getbybarcode, значит введено штрихкод
			getIndustrialSites(curInput, chatId);
			return;
		}
		//обратка бессмысленного ввода в поле, например, когда ожидается ввод команды, а приходит сообщение
		execute(collectAnswer(chatId, "ОШИБКА -> Не знаю, что с этим делать..."));
	}

	@SneakyThrows
	//если пришло команда через кнопку
	private void callBackQueryProcessing(Update update) {
		chatId = update.getCallbackQuery().getMessage().getChatId();
		curCbq = update.getCallbackQuery().getData();

		if (curCbq.equalsIgnoreCase(AUTHORIZATION)) {
			execute(collectAnswer(chatId, "Введите в текстовое поле ваш рабочий email"));
			lastCbq = curCbq;
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_DECLARATION)) {
			kb = InlineKeyboardMarkup.builder()
					.keyboardRow(List.of(getButton("по КОДу ERP", GET_BY_ERP_CODE)))
					.keyboardRow(List.of(getButton("по ШТРИХКОДУ", GET_BY_BARCODE)))
					.build();
			execute(collectAnswer(chatId, "Варианты загрузки -->", kb));
			lastCbq = curCbq;
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_BY_ERP_CODE)) {
			execute(collectAnswer(chatId, "Введите код ЕРП"));
			lastCbq = curCbq;
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_BY_BARCODE)) {
			execute(collectAnswer(chatId, "Введите штрихкод"));
			lastCbq = curCbq;
			return;
		}
		if (lastCbq.equals(GET_BY_BARCODE)) {
			getDeclarationByIndustrialSiteAndBarcode(curCbq, chatId);
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_QUALITY)) {
			execute(collectAnswer(chatId, "Извиняюсь, функционал в стадии разработки"));
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_LABEL_MOCKUP)) {
			execute(collectAnswer(chatId, "ОШИБКА --> Неизвестная команда")); //todo
			return;
		}
		execute(collectAnswer(chatId, "ОШИБКА --> Неизвестная команда"));
	}

	@SneakyThrows
	private void getDeclarationByIndustrialSiteAndBarcode(String indSiteAndBarcode, Long chatId) {
		//под 0 - площадка, под 1 - штрихкод
		String[] data = indSiteAndBarcode.split(",");
		List<Product> products = productRepo.findByIndustrialSiteAndBarcode(data[0], data[1]);
		downloadDeclaration(products);
		execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
	}

	@SneakyThrows
	private void getIndustrialSites(String barcode, Long chatId) {
		if (isStringNumeric(barcode)) { //проверка что сообщение не содержит букв
			execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
			return;
		}
		List<String> industrialSites = productRepo.getIndustrialSitesByProductBarcode(barcode);
		if (industrialSites.isEmpty()) {
			execute(collectAnswer(chatId, "Не найдены пром.площадки для выбора"));
			execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
			return;
		}
		InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> buttonsRows = new ArrayList<>();
		for (int i = 0; i < industrialSites.size(); i++) {
			buttonsRows.add(List.of(getButton(industrialSites.get(i),industrialSites.get(i) + "," + barcode)));
		}
		kb.setKeyboard(buttonsRows);
		execute(collectAnswer(chatId, "Выберите пром.площадку -->", kb));
	}

	@SneakyThrows
	private void getDeclarationByErpCode(String code, Long chatId) {
		if (isStringNumeric(code)) { //проверка что сообщение не содержит букв
				execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
			return;
		}
		Long codeWithoutZero = cutFrontZero(code); // обрезаем впереди стоящие нули
		try {
			Optional<Product> existedProduct = productRepo.findById(codeWithoutZero); //todo почитать по методы чтобы избавиться от EAGER, возможно транзакции спасут
			if (existedProduct.isPresent()) { // если товар найден
				downloadDeclaration(List.of(existedProduct.get()));
				execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
			} else {
				execute(collectAnswer(chatId, "ОШИБКА --> Товар не найден..."));
			}
		} catch (InvalidDataAccessResourceUsageException e) {
				execute(collectAnswer(chatId, "Ошибка --> Проблема с запросом в БД"));
		}
	}

	@SneakyThrows
	private void downloadDeclaration(List<Product> products) {
		for (Product prod : products) {
			List<Declaration> productDeclarations = prod.getDeclarations();
			List<String> pathToFiles = productDeclarations.stream()
					.map(dec -> PATH_DIR_DECLARATIONS + dec.getFileName())
					.toList();
			List<File> files = pathToFiles.stream()
					.map(File::new)
					.toList();
			for (File file : files) {
				if (file.exists() && !file.isDirectory()) {
					execute(SendDocument.builder()
							.chatId(chatId)
							.document(new InputFile(file))
							.build());
				} else {
					execute(collectAnswer(chatId, "ОШИБКА ---> Файл не найден..."));
				}
			}
		}
	}

	@SneakyThrows
	private void doAuthorization(String email) {
		Optional<User> user = userService.getUser(email); //проверка в БД на наличие пользователя
		if (user.isPresent()) {
			chatService.saveChat(chatId, user.get());
			kb = InlineKeyboardMarkup.builder()
					.keyboardRow(List.of(getButton("ДС", GET_DECLARATION),
							getButton("КУ", GET_QUALITY)))
					.build();
			execute(collectAnswer(chatId, "Успешная авторизация!"));
			execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
			lastInput = email;
		} else { // если пользователя нет в БД
			execute(collectAnswer(chatId, "По-моему я вас не знаю...\nДавайте попробуем ещё раз?\nлибо напишите на ekuznecov@ecln.ru"));
		}
	}

	@SneakyThrows
	private void doStart() {
		kb = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(getButton("Авторизоваться", AUTHORIZATION)))
				.build();
		execute(collectAnswer(chatId,
				"Вас приветствует Бот-Документ!\n" + "Давайте познакомимся ?",
				kb));
		lastInput = curInput;
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
			return false;
		} catch (NumberFormatException e) {
			return true;
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
				.keyboardRow(List.of(getButton("Декларация соответствия", GET_DECLARATION)))
				.keyboardRow(List.of(getButton("Качественное удостоверение", GET_QUALITY)))
				.keyboardRow(List.of(getButton("Макет этикетки", GET_LABEL_MOCKUP)))
				.build();
	}
}