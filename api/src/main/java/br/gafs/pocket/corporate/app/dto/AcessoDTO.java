/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.dto;

import br.gafs.pocket.corporate.dto.MenuDTO;
import br.gafs.pocket.corporate.entity.Colaborador;
import br.gafs.pocket.corporate.entity.Usuario;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.dto.DTO;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
public class AcessoDTO implements DTO {
    private Colaborador colaborador;
    private Usuario usuario;
    private List<Funcionalidade> funcionalidades;
    private String token;
    private Integer tipoDispositivo;
    private String version;
    private String auth;
    private MenuDTO menu;

    public AcessoDTO(Colaborador colaborador, List<Funcionalidade> funcionalidades, String auth, MenuDTO menu) {
        this.colaborador = colaborador;
        this.funcionalidades = funcionalidades;
        this.auth = auth;
        this.menu = menu;
    }

    public AcessoDTO(Usuario usuario) {
        this.usuario = usuario;
    }
    
    
}
