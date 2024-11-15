/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.HorasEnvioNotificacao;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroDispositivoNotificacaoDTO implements DTO, Cloneable {
    @JsonView(Resumido.class)
    private List<Long> ministerios = new ArrayList<Long>();
    @JsonIgnore
    private Integer pagina = 1;
    @JsonIgnore
    private TipoDispositivo tipo;
    @JsonView(Resumido.class)
    private HorasEnvioNotificacao hora;
    @JsonView(Resumido.class)
    private Long membro;
    @JsonView(Resumido.class)
    private Igreja igreja;
    @JsonView(Resumido.class)
    private boolean apenasMembros;
    @JsonView(Resumido.class)
    private Date aniversario;
    @JsonView(Resumido.class)
    private boolean desejaReceberNotificacoesVideos;
    @JsonView(Resumido.class)
    private Long idPlanoLeiuraBiblica;
    @JsonView(Resumido.class)
    private boolean devocionario;

    public FiltroDispositivoNotificacaoDTO(Igreja igreja) {
        this.igreja = igreja;
    }

    public FiltroDispositivoNotificacaoDTO(Igreja igreja, boolean desejaReceberNotificacoesVideos) {
        this.igreja = igreja;
        this.desejaReceberNotificacoesVideos = desejaReceberNotificacoesVideos;
    }

    public FiltroDispositivoNotificacaoDTO(Igreja igreja, Date aniversario) {
        this.igreja = igreja;
        this.aniversario = aniversario;
    }

    public FiltroDispositivoNotificacaoDTO(Igreja igreja, Long membro) {
        this(igreja);
        this.membro = membro;
    }
    
    public FiltroDispositivoNotificacaoDTO(Igreja igreja, HorasEnvioNotificacao hora) {
        this(igreja);
        this.hora = hora;
    }
    
    public FiltroDispositivoNotificacaoDTO(Igreja igreja, HorasEnvioNotificacao hora, Long idPlanoLeiuraBiblica) {
        this(igreja);
        this.hora = hora;
        this.idPlanoLeiuraBiblica = idPlanoLeiuraBiblica;
    }
    
    public void proxima(){
        pagina++;
    }

    @Override
    public FiltroDispositivoNotificacaoDTO clone()  {
        try {
            return (FiltroDispositivoNotificacaoDTO) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return this;
        }
    }
    
    
}
