package ru.kevdev.PvDeclarationBot.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {
    @ExceptionHandler(value = BadRequestException.class)
    public ErrorMessage handleBadRequest(BadRequestException exception) {
        //code...
        return errMsg;
    }
}