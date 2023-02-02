/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.dto;

import br.gafs.calvinista.dto.MenuDTO;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Usuario;
import br.gafs.calvinista.entity.domain.Funcionalidade;
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
    private Membro membro;
    private Usuario usuario;
    private List<Funcionalidade> funcionalidades;
    private String token;
    private Integer tipoDispositivo;
    private String version;
    private String auth;
    private MenuDTO menu;

    private boolean exigeAceiteTermo;

    public AcessoDTO(Membro membro, List<Funcionalidade> funcionalidades, String auth,
                     MenuDTO menu, boolean exigeAceiteTermo) {
        this.membro = membro;
        this.funcionalidades = funcionalidades;
        this.auth = auth;
        this.menu = menu;
        this.exigeAceiteTermo = exigeAceiteTermo;
    }

    public AcessoDTO(Usuario usuario) {
        this.usuario = usuario;
    }
    
    
}
