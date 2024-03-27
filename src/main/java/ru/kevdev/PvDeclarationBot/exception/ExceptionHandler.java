package ru.kevdev.PvDeclarationBot.exception;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Aspect
public class ExceptionHandler {
    @AfterThrowing(pointcut = "execution(* ru.kevdev.PvDeclarationBot.*(..))", throwing="e")
    public void handleBadRequest(NotFoundException e) {
        //code...
//        return errMsg;
    }
}