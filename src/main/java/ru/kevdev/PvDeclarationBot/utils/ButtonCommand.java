package ru.kevdev.PvDeclarationBot.utils;

import lombok.Getter;

@Getter
public enum ButtonCommand {
//    START("/start"),
//    AUTHORIZATION("/authorization"),
//    GET_DECLARATION("/getDeclaration"),
//    GET_QUALITY("/getQuality");

    START,
    AUTHORIZATION,
    GET_DECLARATION,
    GET_QUALITY,
    GET_LABEL_MOCKUP,
    BY_ERP_CODE,
    BY_BARCODE;


//    private String text;
//
//    ButtonCommand(String text) {
//        this.text = text;
//    }
}