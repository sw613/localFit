package com.example.LocalFit.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    //CustomException 예외 처리
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<CustomErrorResponseDto> handleCustomException(CustomException e) {
        CustomErrorCode customErrorCode = e.getCustomErrorCode();
        return ResponseEntity
                .status(customErrorCode.getStatus())
                .body(CustomErrorResponseDto.builder()
                        .statusCode(customErrorCode.getStatus().value())
                        .code(customErrorCode.name())
                        .message(customErrorCode.getMessage())
                        .build());
    }

    //@Valid 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMessage.append(fieldError.getField())
                    .append(" : ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        errorMessage.toString()
                ));
    }

    //기타 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<CustomErrorResponseDto> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomErrorResponseDto.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .code("INTERNAL_SERVER_ERROR")
                        .message("서버 내부 오류 발생")
                        .build());
    }
}
