package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.view.View;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Gabriel on 16/02/2018.
 */
@Getter
@Setter
public class EventoCalendarioDTO implements Comparable<EventoCalendarioDTO> {
    private String id;
    @View.JsonTemporal(View.JsonTemporalType.TIMESTAMP)
    private Date inicio;
    @View.JsonTemporal(View.JsonTemporalType.TIMESTAMP)
    private Date termino;
    private String descricao;
    private String local;

    @Override
    public int compareTo(EventoCalendarioDTO o) {
        return inicio.compareTo(o.inicio);
    }
}
