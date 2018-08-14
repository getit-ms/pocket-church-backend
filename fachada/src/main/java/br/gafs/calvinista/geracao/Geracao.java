/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.geracao;

import br.gafs.calvinista.dto.ParametrosIgrejaDTO;
import br.gafs.calvinista.entity.Igreja;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Interface de geração de versões
 * 
 * @author Gabriel
 */
@Getter
public final class Geracao implements Serializable {
    public List<EtapaProcessamento> etapas = new ArrayList<EtapaProcessamento>();
    private String description;
    private File source;
    private StringBuilder console;

    public Geracao(String description, File source) {
        this.description = description;
        this.source = source;
    }
    
    public void add(EtapaProcessamento etapa){
        etapas.add(etapa);
    }
    
    public File execute(Igreja igreja, ParametrosIgrejaDTO params) throws Exception {
        File output = source;
        for (EtapaProcessamento etapa : etapas){
            output = etapa.execute(source, igreja, params, new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    console.append(b);
                }
            });
        }
        return output;
    }
    
    public static Builder builder(String description, File source){
        return new Builder(description, source);
    }
    
    public static class Builder {
        private Geracao geracao;
        
        private Builder(String description, File source){
            geracao = new Geracao(description, source);
        }
        
        public Builder with(EtapaProcessamento etapa){
            geracao.add(etapa);
            return this;
        }
        
        public Geracao build(){
            return geracao;
        }
    }
}
