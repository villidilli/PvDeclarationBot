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
    BY_ERP_CODE,
    BY_BARCODE;


//    private String text;
//
//    ButtonCommand(String text) {
//        this.text = text;
//    }
}