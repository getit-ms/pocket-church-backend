/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.dto;

import br.gafs.pocket.corporate.exception.ValidationException;
import br.gafs.pocket.corporate.exception.ValidationException.Validation;
import br.gafs.dto.DTO;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
public class ErrosDTO implements DTO {
    private String message;
    private Map<String, Object> args = new HashMap<String, Object>();
    private List<ValidationDTO> validations = new ArrayList<ValidationDTO>();
    private String redirectUrl;

    public ErrosDTO(String message) {
        this.message = message;
    }
    
    public ErrosDTO arg(String name, Object value){
        args.put(name, value);
        return this;
    }

    public ErrosDTO(ValidationException ve) {
        this.message = ve.getMessage();
        for (Validation v : ve.getValidations()){
            this.validations.add(new ValidationDTO(v.getField(), v.getMessage(), v.getArgs()));
        }
    }
    
    public ErrosDTO(ServiceExceptionList sel){
        this(sel.get(0));
    }
    
    public ErrosDTO(SecurityException se){
        this.message = se.getMessage();
    }
    
    public ErrosDTO(ServiceException se){
        this.message = se.getMessage();
        int i=0;
        if (se.getArguments() != null){
            for (Object arg : se.getArguments()){
                arg("" + i, arg);
            }
        }
    }

    public ErrosDTO(ConstraintViolationException cve) {
        this(ValidationException.build(cve));
    }
    
    public ErrosDTO add(String field, String message) {
        validations.add(new ValidationDTO(field, message));
        return this;
    }

    public ErrosDTO add(String field, String message, Map<String, Object> args) {
        validations.add(new ValidationDTO(field, message, args));
        return this;
    }
    
    @Getter
    @AllArgsConstructor
    public class ValidationDTO{
        private String field;
        private final String message;
        private Map<String, Object> args;
        
        public ValidationDTO(String field, String message) {
            this(field, message, new HashMap<String, Object>());
        }
        
        
    }
}
