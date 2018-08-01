/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Gabriel
 */
@Getter
@AllArgsConstructor
public class CalvinEmailDTO implements DTO {
    @Setter
    private Manchete manchete;
    private List<Materia> materias = new ArrayList<Materia>();
    
    @Getter
    @AllArgsConstructor
    public static class Manchete implements DTO {
        private String titulo;
        private String texto;
        private String linkURL;
        private String linkTexto;
    }
    
    @Getter
    @AllArgsConstructor
    public static class Materia implements DTO {
        private String titulo;
        private String texto;
    }
    
    @Getter
    public static class MateriaIlustrada extends Materia {
        private File imagem;
        private String linkURL;
        private String linkTexto;

        public MateriaIlustrada(String titulo, String texto, File imagem, String linkURL, String linkTexto) {
            super(titulo, texto);
            this.imagem = imagem;
            this.linkURL = linkURL;
            this.linkTexto = linkTexto;
        }
    }
    
    @Getter
    public static class MateriaDupla extends Materia {
        private String titulo2;
        private String texto2;

        public MateriaDupla(String titulo, String texto, String titulo2, String texto2) {
            super(titulo, texto);
            this.titulo2 = titulo2;
            this.texto2 = texto2;
        }
    }
}
