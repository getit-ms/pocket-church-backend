/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.domain.HorasEnvioNotificacao;
import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import br.gafs.dto.DTO;
import br.gafs.pocket.corporate.view.View;
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
public class FiltroDispositivoNotificacaoDTO implements DTO, Cloneable {
    @JsonView(View.Resumido.class)
    private List<Long> ministerios = new ArrayList<Long>();
    @JsonIgnore
    private Integer pagina = 1;
    @JsonIgnore
    private TipoDispositivo tipo;
    @JsonView(View.Resumido.class)
    private HorasEnvioNotificacao hora;
    @JsonView(View.Resumido.class)
    private Long colaborador;
    @JsonView(View.Resumido.class)
    private Empresa empresa;
    @JsonView(View.Resumido.class)
    private boolean apenasColaboradores;
    @JsonView(View.Resumido.class)
    private Date aniversario;
    @JsonView(View.Resumido.class)
    private boolean desejaReceberNotificacoesVideos;
    @JsonView(View.Resumido.class)
    private Long idPlanoLeiuraBiblica;

    public FiltroDispositivoNotificacaoDTO(Empresa empresa) {
        this.empresa = empresa;
    }

    public FiltroDispositivoNotificacaoDTO(Empresa empresa, boolean desejaReceberNotificacoesVideos) {
        this.empresa = empresa;
        this.desejaReceberNotificacoesVideos = desejaReceberNotificacoesVideos;
    }

    public FiltroDispositivoNotificacaoDTO(Empresa empresa, Date aniversario) {
        this.empresa = empresa;
        this.aniversario = aniversario;
    }

    public FiltroDispositivoNotificacaoDTO(Empresa empresa, Long colaborador) {
        this(empresa);
        this.colaborador = colaborador;
    }
    
    public FiltroDispositivoNotificacaoDTO(Empresa empresa, HorasEnvioNotificacao hora) {
        this(empresa);
        this.hora = hora;
    }
    
    public FiltroDispositivoNotificacaoDTO(Empresa empresa, HorasEnvioNotificacao hora, Long idPlanoLeiuraBiblica) {
        this(empresa);
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
