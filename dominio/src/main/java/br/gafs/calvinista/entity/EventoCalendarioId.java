package br.gafs.calvinista.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "inicio", "chaveIgreja"})
public class EventoCalendarioId implements Serializable {
    private String id;
    private Date inicio;
    private String chaveIgreja;
}
