/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.exception;

import lombok.Getter;

/**
 *
 * @author Gabriel
 */
public class SecurityException extends RuntimeException {
    @Getter
    private String message = "mensagens.MSG-403";
    
}
