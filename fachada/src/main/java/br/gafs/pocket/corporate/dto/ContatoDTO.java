/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ContatoDTO implements DTO {
    private String nome;
    private String email;
    private String telefone;
    private String assunto;
    private String mensagem;
}
