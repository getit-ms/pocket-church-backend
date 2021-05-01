/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.view.View;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
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
    private Igreja igreja;
    @JsonView(View.Resumido.class)
    private Long membro;
    @JsonIgnore
    private Integer pagina = 1;

    public FiltroEmailDTO(Igreja igreja, Long membro) {
        this.igreja = igreja;
        this.membro = membro;
    }

    public void proxima(){
        this.pagina++;
    }
}
