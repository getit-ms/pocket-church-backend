/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.dto;

import br.gafs.dto.DTO;
import br.gafs.util.date.DateUtil;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@RequiredArgsConstructor
public class DateParam implements DTO {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
    
    private final String data;

    public Date getData(){
        if (data != null){
            return DateUtil.parseData(data, DATE_PATTERN);
        }
        return null;
    }
    
}
