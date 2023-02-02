/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tb_marcacao_leitura_biblica")
@NamedQueries({
        @NamedQuery(name = "MarcacaoLeituraBiblica.countLeituraSelecionada", query = "select count(dlb.id) from DiaLeituraBiblica dlb, OpcaoLeituraBiblica olb where (dlb.plano.ultimaAlteracao > :ultimaAlteracao or exists( select mlb from MarcacaoLeituraBiblica mlb where mlb.dia = dlb and olb.membro = mlb.membro and mlb.data > :ultimaAlteracao )) and olb.membro.id = :idMembro and olb.membro.igreja.chave = :chaveIgreja and olb.dataTermino is null and olb.planoLeitura = dlb.plano"),
        @NamedQuery(name = "MarcacaoLeituraBiblica.findLeituraSelecionada", query = "select new br.gafs.calvinista.dto.LeituraBibliaDTO(dlb) from DiaLeituraBiblica dlb, OpcaoLeituraBiblica olb where (dlb.plano.ultimaAlteracao > :ultimaAlteracao or exists( select mlb from MarcacaoLeituraBiblica mlb where mlb.dia = dlb and olb.membro = mlb.membro and mlb.data > :ultimaAlteracao )) and olb.planoLeitura = dlb.plano and olb.membro.id = :idMembro and olb.membro.igreja.chave = :chaveIgreja and olb.dataTermino is null order by dlb.data"),
        @NamedQuery(name = "MarcacaoLeituraBiblica.findByMembroAndDia", query = "select mlb from MarcacaoLeituraBiblica mlb where mlb.membro.id = :idMembro and mlb.membro.igreja.chave = :chaveIgreja and mlb.dia.id = :idDia"),
        @NamedQuery(name = "MarcacaoLeituraBiblica.removeByPlano", query = "delete from MarcacaoLeituraBiblica mlb where mlb.dia.plano.id = :idPlano and mlb.dia.plano.igreja.chave = :chaveIgreja")
})
public class MarcacaoLeituraBiblica implements IEntity {
    @Id
    @Column(name = "id_marcacao_leitura_biblica")
    @GeneratedValue(generator = "seq_marcacao_leitura_biblica", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_marcacao_leitura_biblica", sequenceName = "seq_marcacao_leitura_biblica")
    private Long id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private Membro membro;

    @ManyToOne
    @JoinColumn(name = "id_dia_leitura_biblica")
    private DiaLeituraBiblica dia;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data")
    private Date data = new Date();

    public MarcacaoLeituraBiblica(Membro membro, DiaLeituraBiblica dia) {
        this.membro = membro;
        this.dia = dia;
    }
}
