/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusAgendamentoAtendimento;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.TimeZone;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
@Table(name = "tb_agendamento_atendimento")
@NamedQueries({
    @NamedQuery(name = "AgendamentoAtendimento.findByStatusCalendarioPeriodo",
            query = "select aa from AgendamentoAtendimento aa where aa.empresa.chave = :chaveEmpresa and aa.calendario.id = :idCalendario and"
                    + " aa.dataHoraInicio >= :dataInicio and aa.dataHoraInicio <= :dataTermino and aa.status in :status order by aa.dataHoraInicio"),
    @NamedQuery(name = "AgendamentoAtendimento.findAgendamentoEmChoque",
            query = "select aa from AgendamentoAtendimento aa where aa.calendario.id = :idCalendario and aa.status in :status and"
                    + " aa.dataHoraInicio < :dataTermino and aa.dataHoraFim > :dataInicio")
})
public class AgendamentoAtendimento implements IEntity {
    @Id
    @Column(name = "id_agendamento_atendimento")
    @GeneratedValue(generator = "seq_agendamento_atendimento", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_agendamento_atendimento", sequenceName = "seq_agendamento_atendimento")
    private Long id;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false, insertable = false, updatable = false)
    })
    private Colaborador colaborador;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_calendario_atendimento", referencedColumnName = "id_calendario_atendimento", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false, insertable = false, updatable = false)
    })
    private CalendarioAtendimento calendario;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_hora_inicio", nullable = false)
    private Date dataHoraInicio;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_hora_fim", nullable = false)
    private Date dataHoraFim;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusAgendamentoAtendimento status = StatusAgendamentoAtendimento.NAO_CONFIRMADO;
    
    public AgendamentoAtendimento(Colaborador colaborador, HorarioAtendimento horario, Date data){
        this.colaborador = colaborador;
        this.empresa = colaborador.getEmpresa();
        this.calendario = horario.getCalendario();
        
        TimeZone timeZone = TimeZone.getTimeZone(empresa.getTimezone());
        this.dataHoraInicio = horario.getInicio(timeZone, data);
        this.dataHoraFim = horario.getFim(timeZone, data);
    }
    
    public void confirmado(){
        status = StatusAgendamentoAtendimento.CONFIRMADO;
    }
    
    public void cancelado(){
        status = StatusAgendamentoAtendimento.CANCELADO;
    }
    
    public boolean isConcluido(){
        return isConfirmado() && DateUtil.getDataAtual().after(dataHoraFim);
    }
    
    public boolean isNaoConfirmado(){
        return StatusAgendamentoAtendimento.NAO_CONFIRMADO.equals(status);
    }
    
    public boolean isConfirmado(){
        return StatusAgendamentoAtendimento.CONFIRMADO.equals(status);
    }
    
    public boolean isCancelado(){
        return StatusAgendamentoAtendimento.CANCELADO.equals(status);
    }
}
