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
@EqualsAndHashCode(of = {"igreja", "data", "funcionalidade"})
public class EstatisticaAcessoId implements Serializable {
    private String igreja;
    private Date data;
    private Integer funcionalidade;
}
