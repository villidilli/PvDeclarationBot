package ru.kevdev.PvDeclarationBot.utils;

import lombok.Getter;

@Getter
public enum ButtonCommand {
    START,
    AUTHORIZATION,
    GET_DECLARATION,
    GET_QUALITY,
    GET_LABEL_MOCKUP,
    BY_ERP_CODE,
    BY_BARCODE;
}