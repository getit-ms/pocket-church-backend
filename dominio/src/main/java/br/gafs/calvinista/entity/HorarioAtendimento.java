/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.DiaSemana;
import br.gafs.calvinista.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_horario_atendimento")
@NamedQueries({
    @NamedQuery(name = "HorarioAtendimento.findByCalendarioAndPeriodo", query = "select ha from HorarioAtendimento ha where ha.calendario.id = :idCalendario and (ha.dataInicio is null or ha.dataInicio < :dataFim) and (ha.dataFim is null or ha.dataFim > :dataInicio)")
})
public class HorarioAtendimento implements IEntity {
    @Id
    @Column(name = "id_horario_atendimento")
    @GeneratedValue(generator = "seq_horario_atendimento", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_horario_atendimento", sequenceName = "seq_horario_atendimento")
    private Long id;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumns({
        @JoinColumn(name = "id_calendario_atendimento", referencedColumnName = "id_calendario_atendimento"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private CalendarioAtendimento calendario;
    
    @Setter
    @JsonIgnore
    @Column(name = "dia_semana", nullable = false)
    private Integer diasSemana;
    
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_cadastro", nullable = false)
    private Date dataCadastro = new Date();

    @JsonIgnore
    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;

    @JsonIgnore
    @Column(name = "hora_fim", nullable = false)
    private String horaFim;
    
    @Setter
    @Temporal(TemporalType.DATE)
    @Column(name = "data_inicio")
    private Date dataInicio;
    
    @Setter
    @Temporal(TemporalType.DATE)
    @Column(name = "data_fim")
    private Date dataFim;

    public HorarioAtendimento(CalendarioAtendimento calendario) {
        this.calendario = calendario;
    }

    @JsonProperty
    @View.JsonTemporal(View.JsonTemporalType.TIME)
    public Date getHoraInicio() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaInicio.substring(0, 2)));
        cal.set(Calendar.MINUTE, Integer.parseInt(horaInicio.substring(3, 5)));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @JsonProperty
    @View.JsonTemporal(View.JsonTemporalType.TIME)
    public Date getHoraFim() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaFim.substring(0, 2)));
        cal.set(Calendar.MINUTE, Integer.parseInt(horaFim.substring(3, 5)));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public void setHoraFim(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        this.horaFim = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    public void setHoraInicio(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        this.horaInicio = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }
    
    public Date getInicio(TimeZone timeZone, Date dataMerge){
        return merge(timeZone, dataMerge, horaInicio);
    }
    
    public Date getFim(TimeZone timeZone, Date dataMerge){
        return merge(timeZone, dataMerge, horaFim);
    }
    
    public HorarioAtendimento copy(){
        HorarioAtendimento copy = new HorarioAtendimento(calendario);
        copy.horaInicio = horaInicio;
        copy.horaFim = horaFim;
        copy.diasSemana = diasSemana;
        copy.dataFim = dataFim;
        copy.dataInicio = dataInicio;
        return copy;
    }
    
    private Date merge(TimeZone timeZone, Date date, String time) {
        Calendar dateCal = Calendar.getInstance(timeZone);
        dateCal.setTime(date);
        
        // Extract the time of the "time" object to the "date"
        dateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
        dateCal.set(Calendar.MINUTE, Integer.parseInt(time.substring(3, 5)));
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);

        // Get the time value!
        return dateCal.getTime();
    }
    
    @JsonProperty
    public List<DiaSemana> getDiasSemana(){
        return DiaSemana.values(diasSemana);
    }
    
    @JsonProperty
    public void setDiasSemana(List<DiaSemana> diasSemana){
        this.diasSemana = DiaSemana.valueOf(diasSemana);
    }

    public boolean contains(Calendar cal) {
        for (DiaSemana ds : getDiasSemana()){
            if (ds.dia() == cal.get(Calendar.DAY_OF_WEEK)){
                if (dataInicio != null && dataInicio.after(cal.getTime())){
                    return false;
                }
                if (dataFim != null && dataFim.before(cal.getTime())){
                    return false;
                }
                return true;
            }
        }
        
        
        return false;
    }

    public void removeDiaSemana(DiaSemana ds) {
        diasSemana = ds.unset(diasSemana);
    }
    
}
