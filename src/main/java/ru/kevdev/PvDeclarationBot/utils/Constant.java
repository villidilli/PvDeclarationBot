package ru.kevdev.PvDeclarationBot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
    public final String DATE_TIME_FORMAT = "yyyy-MM-dd";
    public final String PATH_DIR_DECLARATIONS =
//            "sftp://root@185.112.102.46/home/myfiles/declarations/";
            "/var/lib/docker/";
    public final String ERROR_INPUT_NOT_NUM = "ОШИБКА --> Введенное значение не является числом";
    public final String START = "/start";
    public final String AUTHORIZATION = "AUTHORIZATION";
    public final String GET_DECLARATION = "GET_DECLARATION";
    public final String GET_QUALITY = "GET_QUALITY";
    public final String GET_LABEL_MOCKUP = "GET_LABEL_MOCKUP";
    public final String GET_BY_ERP_CODE = "GET_BY_ERP_CODE";
    public final String GET_BY_BARCODE = "GET_BY_BARCODE";
}