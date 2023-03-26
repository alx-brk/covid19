package com.example.covid19stat.controller;


import com.example.covid19stat.exception.InvalidRequestException;
import com.example.openapi.samples.gen.springbootserver.model.CommonError;
import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(annotations = RestController.class)
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<CommonError> handleInvalidRequestException(InvalidRequestException exception) {
        return getResponse(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<CommonError> handleDataAccessException(DataAccessException exception) {
        return getResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonError> handleRuntimeException(RuntimeException exception) {
        return getResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<CommonError> getResponse(RuntimeException exception, HttpStatus status) {
        CommonError error = new CommonError()
                .error(exception.getClass().getSimpleName())
                .message(exception.getMessage());
        logger.error(error, exception);
        return new ResponseEntity<>(error, status);
    }
}
