/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.view.View;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FiltroEmailDTO implements DTO {
    @JsonView(View.Resumido.class)
    private Empresa empresa;
    @JsonView(View.Resumido.class)
    private Long colaborador;
    @JsonIgnore
    private Integer pagina = 1;

    public FiltroEmailDTO(Empresa empresa, Long colaborador) {
        this.empresa = empresa;
        this.colaborador = colaborador;
    }

    public void proxima(){
        this.pagina++;
    }
}
