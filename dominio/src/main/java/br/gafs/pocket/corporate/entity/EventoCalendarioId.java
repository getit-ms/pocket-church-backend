package br.gafs.pocket.corporate.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "inicio", "chaveEmpresa"})
public class EventoCalendarioId implements Serializable {
    private String id;
    private Date inicio;
    private String chaveEmpresa;
}

