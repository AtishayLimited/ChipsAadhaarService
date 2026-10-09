package com.chips.aadhaar.controller;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> status(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("status",ex.getStatusCode().value(),"message",Objects.toString(ex.getReason(),"Request failed")));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex) {
        Map<String,String> fields=new LinkedHashMap<>();ex.getBindingResult().getFieldErrors().forEach(e->fields.put(e.getField(),Objects.toString(e.getDefaultMessage(),"Invalid")));
        return ResponseEntity.badRequest().body(Map.of("status",400,"message","Validation failed","fields",fields));
    }
}
