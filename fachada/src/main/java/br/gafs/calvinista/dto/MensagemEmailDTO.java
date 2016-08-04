/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class MensagemEmailDTO implements DTO, Cloneable {
    @NonNull
    private String subject;
    @NonNull
    private String message;
    private Anexo[] attachments;
    private String[] attachmentsNames;
    
    @JsonIgnore
    public DataSource[] getDataSources(){
        DataSource[] ds = new DataSource[attachments.length];
        for (int i=0;i<attachments.length;i++){
            ds[i] = attachments[i].toDataSource();
        }
        return ds;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anexo implements DTO {
        private String valor;
        private TipoAnexo tipo;
        
        public DataSource toDataSource(){
            return tipo.toDataSource(valor);
        }
    }
    
    public enum TipoAnexo {
        URL{

            @Override
            DataSource toDataSource(String valor) {
                try {
                    return new URLDataSource(new URL(valor));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(MensagemEmailDTO.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
            
        },
        ARQUIVO{

            @Override
            DataSource toDataSource(String valor) {
                return new FileDataSource(valor);
            }
            
        };
        
        abstract DataSource toDataSource(String valor);
    }
    
}
