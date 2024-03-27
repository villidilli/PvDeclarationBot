package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kevdev.PvDeclarationBot.exception.InvalidUserInputException;
import ru.kevdev.PvDeclarationBot.model.Bot;
import ru.kevdev.PvDeclarationBot.model.Declaration;
import ru.kevdev.PvDeclarationBot.model.Product;
import ru.kevdev.PvDeclarationBot.model.User;
import ru.kevdev.PvDeclarationBot.repo.ProductRepo;

import java.io.File;
import java.util.*;
import static ru.kevdev.PvDeclarationBot.utils.Constant.*;

@Service
@RequiredArgsConstructor
public class BotService {
	private final UserService userService;
	private final ChatService chatService;
	private final ProductRepo productRepo;
	private Long chatId;
	private String lastInput;
	private String curInput;
	private String lastCbq;
	private String curCbq;
	private InlineKeyboardMarkup kb;

	@SneakyThrows
	// есть пришло сообщение через поле ввода
	public void userInputProcessing(Bot bot, Update update) {
		chatId = update.getMessage().getChatId();
		curInput = update.getMessage().getText();

		if (curInput.equalsIgnoreCase(START)) { // обработка стартовой команды
			doStart(bot);
			return;
		}
		if (lastCbq != null && lastCbq.equals(AUTHORIZATION)) { //если последняя команда авторизация, значит введена почта
			bot.execute(collectAnswer(chatId, "Уже ищу вас..."));
			doAuthorization(bot, curInput);
			return;
		}
		if (lastCbq != null && lastCbq.equals(GET_DECL_BY_ERP_CODE)) {
			bot.execute(collectAnswer(chatId, "Загружаю файл..."));
			getDeclarationByErpCode(bot, curInput, chatId);
			return;
		}
		if (lastCbq != null && lastCbq.equals(GET_DECL_BY_BARCODE)) { //если посл.команда getbybarcode, значит введено штрихкод
			getIndustrialSites(bot, curInput, chatId);
			return;
		}
		if (lastCbq != null && lastCbq.equals(GET_MOCK_BY_ERP_CODE)) {
			bot.execute(collectAnswer(chatId, "Загружаю файл..."));
			getLabelMockupsByErpCode(bot, curInput, chatId);
			return;
		}
		//обратка бессмысленного ввода в поле, например, когда ожидается ввод команды, а приходит сообщение
		bot.execute(collectAnswer(chatId, BAD_INPUT));
	}

	@SneakyThrows
	//если пришло команда через кнопку
	public void callBackQueryProcessing(Bot bot, Update update) {
		chatId = update.getCallbackQuery().getMessage().getChatId();
		curCbq = update.getCallbackQuery().getData();

		if (curCbq.equalsIgnoreCase(AUTHORIZATION)) {
			bot.execute(collectAnswer(chatId, "Введите в текстовое поле ваш рабочий email"));
			lastCbq = curCbq;
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_DECLARATION)) {
			kb = InlineKeyboardMarkup.builder()
					.keyboardRow(List.of(getButton("по КОДу ERP", GET_DECL_BY_ERP_CODE)))
					.keyboardRow(List.of(getButton("по ШТРИХКОДУ", GET_DECL_BY_BARCODE)))
					.build();
			bot.execute(collectAnswer(chatId, "Варианты загрузки -->", kb));
			lastCbq = curCbq;
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_DECL_BY_ERP_CODE)) {
			bot.execute(collectAnswer(chatId, "Введите код ЕРП"));
			lastCbq = curCbq;
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_DECL_BY_BARCODE)) {
			bot.execute(collectAnswer(chatId, "Введите штрихкод"));
			lastCbq = curCbq;
			return;
		}
		if (lastCbq.equals(GET_DECL_BY_BARCODE)) {
			getDeclarationByIndustrialSiteAndBarcode(bot, curCbq, chatId);
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_QUALITY)) {
			bot.execute(collectAnswer(chatId, "Извиняюсь, функционал в стадии разработки"));
			return;
		}
		if (curCbq.equalsIgnoreCase(GET_LABEL_MOCKUP)) {
			kb = InlineKeyboardMarkup.builder()
					.keyboardRow(List.of(getButton("по КОДу ERP", GET_MOCK_BY_ERP_CODE)))
					.keyboardRow(List.of(getButton("по ШТРИХКОДУ", GET_MOCK_BY_BARCODE)))
					.build();
			bot.execute(collectAnswer(chatId, "Варианты загрузки -->", kb));
			lastCbq = curCbq;
			return;
		}
		bot.execute(collectAnswer(chatId, "ОШИБКА --> Неизвестная команда"));
	}

	@SneakyThrows
	private void getDeclarationByIndustrialSiteAndBarcode(Bot bot, String indSiteAndBarcode, Long chatId) {
		//под 0 - площадка, под 1 - штрихкод
		String[] data = indSiteAndBarcode.split(",");
		List<Product> products = productRepo.findByIndustrialSiteAndBarcode(data[0], data[1]);
		downloadDeclaration(bot, products);
		bot.execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
	}

	@SneakyThrows
	private void getIndustrialSites(Bot bot, String barcode, Long chatId) {
		if (isStringNotNumeric(barcode)) { //проверка что сообщение не содержит букв
			bot.execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
			return;
		}
		List<String> industrialSites = productRepo.getIndustrialSitesByProductBarcode(barcode);
		if (industrialSites.isEmpty()) {
			bot.execute(collectAnswer(chatId, "Не найдены пром.площадки для выбора"));
			bot.execute(collectAnswer(chatId, "\nВыберите документ -->", getDocumentTypesKeyboard()));
			return;
		}
		InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> buttonsRows = new ArrayList<>();
		for (int i = 0; i < industrialSites.size(); i++) {
			buttonsRows.add(List.of(getButton(industrialSites.get(i),industrialSites.get(i) + "," + barcode)));
		}
		kb.setKeyboard(buttonsRows);
		bot.execute(collectAnswer(chatId, "Выберите пром.площадку -->", kb));
	}

	@SneakyThrows
	private void getDeclarationByErpCode(Bot bot, String code, Long chatId) {
		if (isStringNotNumeric(code)) { //проверка что сообщение не содержит букв
				bot.execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
			return;
		}
		Long codeWithoutZero = cutFrontZero(code); // обрезаем впереди стоящие нули
		try {
			Optional<Product> existedProduct = productRepo.findById(codeWithoutZero); //todo почитать по методы чтобы избавиться от EAGER, возможно транзакции спасут
			if (existedProduct.isPresent()) { // если товар найден
				downloadDeclaration(bot, List.of(existedProduct.get()));
				bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
			} else {
				bot.execute(collectAnswer(chatId, ERROR_PRODUCT_NOT_FOUND));
				bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
			}
		} catch (InvalidDataAccessResourceUsageException e) {
				bot.execute(collectAnswer(chatId, ERROR_BAD_SQL));
				bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
		}
	}

	@SneakyThrows
	private Optional<Product> gp(Bot bot, String code, Long chatId) {
		while (true) {
			try {
				isStringNotNumeric(code);
				Long codeWithoutZero = cutFrontZero(code); // обрезаем впереди стоящие нули
				return productRepo.findById(codeWithoutZero); //todo почитать по методы чтобы избавиться от EAGER, возможно транзакции спасут
			} catch (InvalidUserInputException e) {
				bot.execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
				bot.execute(collectAnswer(chatId, "Повторите попытку ===>"));
				break;
			} catch (InvalidDataAccessResourceUsageException e) {
				bot.execute(collectAnswer(chatId, ERROR_BAD_SQL));
				bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
				break;
			}
		}
		return Optional.empty();
	}

	@SneakyThrows
	private void getLabelMockupsByErpCode(Bot bot, String code, Long chatId) {
		if (isStringNotNumeric(code)) { //проверка что сообщение не содержит букв
			bot.execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
			return;
		}
		Long codeWithoutZero = cutFrontZero(code); // обрезаем впереди стоящие нули
		try {
			Optional<Product> existedProduct = productRepo.findById(codeWithoutZero); //todo почитать по методы чтобы избавиться от EAGER, возможно транзакции спасут
			if (existedProduct.isPresent()) { // если товар найден
				downloadDeclaration(bot, List.of(existedProduct.get()));
				bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
			} else {
				bot.execute(collectAnswer(chatId, ERROR_PRODUCT_NOT_FOUND));
				bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
			}
		} catch (InvalidDataAccessResourceUsageException e) {
			bot.execute(collectAnswer(chatId, ERROR_BAD_SQL));
			bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
		}

	} //TODO

	@SneakyThrows
	private void downloadDeclaration(Bot bot, List<Product> products) {
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
					bot.execute(SendDocument.builder()
							.chatId(chatId)
							.document(new InputFile(file))
							.build());
				} else {
					bot.execute(collectAnswer(chatId, ERROR_FILE_NOT_FOUND));
					bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
				}
			}
		}
	}

	@SneakyThrows
	private void doAuthorization(Bot bot, String email) {
		Optional<User> user = userService.getUser(email); //проверка в БД на наличие пользователя
		if (user.isPresent()) {
			chatService.saveChat(chatId, user.get());
			kb = InlineKeyboardMarkup.builder()
					.keyboardRow(List.of(getButton("ДС", GET_DECLARATION),
							getButton("КУ", GET_QUALITY)))
					.build();
			bot.execute(collectAnswer(chatId, "Успешная авторизация!"));
			bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
			lastInput = email;
		} else { // если пользователя нет в БД
			bot.execute(collectAnswer(chatId, BAD_AUTHORIZATION));
		}
	}

	@SneakyThrows
	private void doStart(Bot bot) {
		kb = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(getButton("Авторизоваться", AUTHORIZATION)))
				.build();
		bot.execute(collectAnswer(chatId,
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

	private void isStringNotNumeric(String string) {
		try {
			Long.parseLong(string);
		} catch (NumberFormatException e) {
			throw new InvalidUserInputException("Введенное значение не является числом");
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