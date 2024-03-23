package ru.kevdev.PvDeclarationBot.utils;

import lombok.Getter;

@Getter
public enum CbqCommand {
    START,
    AUTHORIZATION,
    GET_DECLARATION,
    GET_QUALITY,
    GET_LABEL_MOCKUP,
    GET_BY_ERP_CODE,
    GET_BY_BARCODE;
}