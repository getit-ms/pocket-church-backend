/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.HorasEnvioVersiculo;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
public class FiltroDispositivoDTO implements DTO {
    @JsonView(Resumido.class)
    private List<Long> ministerios = new ArrayList<Long>();
    @JsonIgnore
    private Integer pagina = 1;
    @JsonIgnore
    private TipoDispositivo tipo;
    @JsonView(Resumido.class)
    private HorasEnvioVersiculo hora;
    @JsonView(Resumido.class)
    private Long membro;
    @JsonView(Resumido.class)
    private Igreja igreja;
    @JsonView(Resumido.class)
    private boolean apenasMembros;
    @JsonView(Resumido.class)
    private Date aniversario;

    public FiltroDispositivoDTO(Igreja igreja) {
        this.igreja = igreja;
    }

    public FiltroDispositivoDTO(Igreja igreja, Date aniversario) {
        this.igreja = igreja;
        this.aniversario = aniversario;
    }

    public FiltroDispositivoDTO(Igreja igreja, Long membro) {
        this(igreja);
        this.membro = membro;
    }
    
    public FiltroDispositivoDTO(Igreja igreja, HorasEnvioVersiculo hora) {
        this(igreja);
        this.hora = hora;
    }
    
    public void proxima(){
        pagina++;
    }
}
