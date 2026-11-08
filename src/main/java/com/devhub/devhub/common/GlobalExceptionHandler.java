package com.devhub.devhub.common;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException e){
        return ResponseEntity.status(e.getStatus())
                .body(ErrorResponse.of(e.getField(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e){
        Map<String, List<String>> errors = new HashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()){
            errors.computeIfAbsent(fe.getField(), k -> new ArrayList<>())
                    .add(fe.getDefaultMessage());}
        return ResponseEntity.badRequest().body(new ErrorResponse(false, errors));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooBig(MaxUploadSizeExceededException e){
        return ResponseEntity.badRequest().body(ErrorResponse.of("avatar", "Файл больше 2МБ"));
    }
}
