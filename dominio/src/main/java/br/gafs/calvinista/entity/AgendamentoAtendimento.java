/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusAgendamentoAtendimento;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Time;
import java.util.Calendar;
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
import lombok.Getter;
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
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_agendamento_atendimento")
@NamedQueries({
    @NamedQuery(name = "AgendamentoAtendimento.findByStatusCalendarioPeriodo",
            query = "select aa from AgendamentoAtendimento aa where aa.igreja.chave = :chaveIgreja and aa.calendario.id = :idCalendario and"
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
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @ManyToOne
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_membro", referencedColumnName = "id_membro", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false, insertable = false, updatable = false)
    })
    private Membro membro;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_calendario_atendimento", referencedColumnName = "id_calendario_atendimento", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false, insertable = false, updatable = false)
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
    
    public AgendamentoAtendimento(Membro membro, HorarioAtendimento horario, Date data){
        this.membro = membro;
        this.igreja = membro.getIgreja();
        this.calendario = horario.getCalendario();
        
        TimeZone timeZone = TimeZone.getTimeZone(igreja.getTimezone());
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
