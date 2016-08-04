/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationException extends RuntimeException {
    @Getter
    private String message = "mensagens.MSG-002";
    
    @Getter
    private List<Validation> validations = new ArrayList<Validation>();
    
    public static ValidationException build(){
        return new ValidationException();
    }
    
    public ValidationException add(String field, String message){
        return add(field, message, null);
    }
    
    public ValidationException add(String field, String message, Map<String, Object> args){
        validations.add(new Validation(field, message, args));
        return this;
    }
    
    public void validate(){
        if (!validations.isEmpty()){
            throw this;
        }
    }
    
    public static ValidationException build(ConstraintViolationException cve){
        ValidationException ve = build();
        for (ConstraintViolation cv : cve.getConstraintViolations()){
            ve.add(cv.getPropertyPath().toString(), cv.getMessage());
        }
        return ve;
    }
    
    @Getter
    @AllArgsConstructor
    public class Validation {
        private String field;
        private String message;
        private Map<String, Object> args;
    }
    
}
