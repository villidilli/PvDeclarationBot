package ru.kevdev.PvDeclarationBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kevdev.PvDeclarationBot.model.*;
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

	// есть пришло сообщение через поле ввода
	public void userInputProcessing(Bot bot, Update update) throws TelegramApiException {
		chatId = update.getMessage().getChatId();
		curInput = update.getMessage().getText();

		if (curInput.equalsIgnoreCase(START)) { // обработка стартовой команды
			doStart(bot);
			return;
		}
		if (lastCbq.equals(AUTHORIZATION)) { //если последняя команда авторизация, значит введена почта
			doAuthorization(bot, curInput);
			return;
		}
		if (lastCbq.equals(GET_DECL_BY_ERP_CODE ) || lastCbq.equals(GET_MOCK_BY_ERP_CODE)) {
			getDocumentsByErpCode(lastCbq, bot, curInput, chatId);
			return;
		}
		if (lastCbq.equals(GET_DECL_BY_BARCODE)) { //если посл.команда getbybarcode, значит введено штрихкод
			getIndustrialSites(bot, curInput, chatId);
			return;
		}
		//обратка бессмысленного ввода в поле, например, когда ожидается ввод команды, а приходит сообщение
		bot.execute(collectAnswer(chatId, BAD_INPUT));
	}


	//если пришло команда через кнопку
	public void callBackQueryProcessing(Bot bot, Update update) throws TelegramApiException {
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
			if (curCbq.equalsIgnoreCase(GET_DECL_BY_ERP_CODE) || curCbq.equalsIgnoreCase(GET_MOCK_BY_ERP_CODE)) {
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


	private void getDeclarationByIndustrialSiteAndBarcode(Bot bot, String indSiteAndBarcode, Long chatId)
																						throws TelegramApiException {
		//под 0 - площадка, под 1 - штрихкод
		String[] data = indSiteAndBarcode.split(",");
		List<Product> products = productRepo.findByIndustrialSiteAndBarcode(data[0], data[1]);
		for (Product prod : products) {
			List<Declaration> declarations = prod.getDeclarations();
			downloadFile(bot, declarations, PATH_DIR_DECLARATIONS);
		}
		bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
	}

	private void getIndustrialSites(Bot bot, String barcode, Long chatId) throws TelegramApiException {
		if (isStringNotNumeric(barcode)) { //проверка что сообщение не содержит букв
			bot.execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
			return;
		}
		List<String> industrialSites = productRepo.getIndustrialSitesByProductBarcode(barcode);
		if (industrialSites.isEmpty()) {
			bot.execute(collectAnswer(chatId, "Не найдены пром.площадки для выбора"));
			bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
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

	private void getDocumentsByErpCode(String lastCbq, Bot bot, String code, Long chatId) throws TelegramApiException {
		bot.execute(collectAnswer(chatId, "Ищу и проверяю файл..."));
		if (isStringNotNumeric(code)) { //проверка что сообщение не содержит букв
			bot.execute(collectAnswer(chatId, ERROR_INPUT_NOT_NUM));
			bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
			return;
		}
		Long codeWithoutZero = cutFrontZero(code); // обрезаем впереди стоящие нули
		Optional<Product> existedProduct = productRepo.findById(codeWithoutZero); //todo почитать по методы чтобы избавиться от EAGER, возможно транзакции спасут
		if (existedProduct.isPresent()) { // если товар найден
			switch (lastCbq) {
				case GET_DECL_BY_ERP_CODE: downloadFile(bot, existedProduct.get().getDeclarations(), PATH_DIR_DECLARATIONS); break;
				case GET_MOCK_BY_ERP_CODE: downloadFile(bot, existedProduct.get().getLabelMockups(), PATH_DIR_LABEL_MOCKUPS); break;
			}
		} else {
			bot.execute(collectAnswer(chatId, ERROR_PRODUCT_NOT_FOUND));
		}
		bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
	}

	private void downloadFile(Bot bot, List<? extends Document> documents, String pathToDir) throws TelegramApiException {
		List<String> pathToFiles = documents.stream()
				.map(document -> pathToDir + document.getFileName())
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

	private void doAuthorization(Bot bot, String email) throws TelegramApiException {
		bot.execute(collectAnswer(chatId, "Уже ищу вас..."));
		Optional<User> user = userService.getUser(email); //проверка в БД на наличие пользователя
		if (user.isPresent()) {
			chatService.saveChat(chatId, user.get());
			bot.execute(collectAnswer(chatId, "Успешная авторизация!"));
			bot.execute(collectAnswer(chatId, SELECT_DOCUMENT, getDocumentTypesKeyboard()));
			lastInput = email;
		} else { // если пользователя нет в БД
			bot.execute(collectAnswer(chatId, BAD_AUTHORIZATION));
		}
	}

	private void doStart(Bot bot) throws TelegramApiException {
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

	private boolean isStringNotNumeric(String string) {
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