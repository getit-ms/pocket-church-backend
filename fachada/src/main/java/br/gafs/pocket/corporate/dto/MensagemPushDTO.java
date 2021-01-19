/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemPushDTO implements DTO, Cloneable {
    private String title;
    private String message;
    private Integer badge;
    private String icon;
    private String sound;
    private Map<String, Object> customData = new HashMap<String, Object>();

    @Override
    public MensagemPushDTO clone() {
        try {
            return (MensagemPushDTO) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return this;
        }
    }
    
    
}
