package ru.kevdev.PvDeclarationBot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
    public final String DATE_TIME_FORMAT = "yyyy-MM-dd";
    public final String PATH_DIR_DECLARATIONS = "/var/lib/docker/volumes/decl_data/"; // смонтированная папка из хоста
    public final String PATH_DIR_LABEL_MOCKUPS = "/var/lib/docker/volumes/mockups_data/"; // смонтированная папка из хоста
    public final String ERROR_INPUT_NOT_NUM = "ОШИБКА --> Введенное значение не является числом";
    public final String SELECT_DOCUMENT = "--------------------\nВыберите документ --->";
    public final String ERROR_BAD_SQL = "Ошибка --> Проблема с запросом в БД";
    public final String ERROR_PRODUCT_NOT_FOUND = "ОШИБКА --> Товар не найден...";
    public final String ERROR_FILE_NOT_FOUND = "ОШИБКА --> Файл не найден...";
    public final String BAD_AUTHORIZATION = "По-моему я вас не знаю...\nДавайте попробуем ещё раз?\nлибо напишите на ekuznecov@ecln.ru";
    public final String BAD_INPUT = "ОШИБКА -> Не знаю, что с этим делать...";
    public final String START = "/start";
    public final String AUTHORIZATION = "AUTHORIZATION";
    public final String GET_DECLARATION = "GET_DECLARATION";
    public final String GET_QUALITY = "GET_QUALITY";
    public final String GET_LABEL_MOCKUP = "GET_LABEL_MOCKUP";
    public final String GET_DECL_BY_ERP_CODE = "GET_DECL_BY_ERP_CODE";
    public final String GET_DECL_BY_BARCODE = "GET_DECL_BY_BARCODE";
    public final String GET_MOCK_BY_ERP_CODE = "GET_MOCK_BY_ERP_CODE";
    public final String GET_MOCK_BY_BARCODE = "GET_MOCK_BY_BARCODE";
}