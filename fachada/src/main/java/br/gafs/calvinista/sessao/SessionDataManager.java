/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.sessao;

import java.io.Serializable;

/**
 *
 * @author Gabriel
 */
public interface SessionDataManager extends Serializable {
    String header(String key);
    String parameter(String key);
    void header(String key, String value);
    
}
