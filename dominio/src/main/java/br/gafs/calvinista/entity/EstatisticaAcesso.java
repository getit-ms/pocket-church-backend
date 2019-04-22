package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EstatisticaAcessoId.class)
@Table(name = "tb_estatistica_dispositivo")
@EqualsAndHashCode(of = {"igreja", "data", "funcionalidade"})
@NamedQueries({
        @NamedQuery(name = "EstatisticaAcesso.findByIgrejaAndFuncionalidade", query = "select ra from EstatisticaAcesso ra where ra.igreja = :igerja and ra.funcionalidade = :funcionalidade order by ra.data"),
        @NamedQuery(name = "EstatisticaAcesso.findOnLine", query = "select new br.gafs.calvinista.entity.EstatisticaAcesso(i, CURRENT_DATE, ra.funcionalidade, (select count(*) from RegistroAcesso su where su.igreja = ra.igreja and su.acesso between :inicio and :termino and su.funcionalidade = ra.funcionalidade and su.status = :sucesso), (select count(*) from RegistroAcesso su where su.igreja = ra.igreja and su.acesso between :inicio and :termino and su.funcionalidade = ra.funcionalidade and su.status = :falha)) from RegistroAcesso ra Igreja i where i.chave = ra.igreja and i.status = :statusIgreja group by i, ra.funcionalidade "),
        @NamedQuery(name = "EstatisticaAcesso.removeAntigas", query = "delete from EstatisticaAcesso e where e.data < :limite")
})
public class EstatisticaAcesso implements IEntity {
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Id
    @Column(name = "data")
    @Temporal(TemporalType.DATE)
    @View.JsonTemporal(View.JsonTemporalType.DATE)
    private Date data;

    @Id
    @JsonIgnore
    @Column(name = "funcionalidade")
    private Integer funcionalidade;

    @Column(name = "quantidade_acessos_sucesso")
    private Long quantidadeAcessosSucesso;

    @Column(name = "quantidade_acessos_falhos")
    private Long quantidadeAcessosFalhos;

    @Override
    public EstatisticaAcessoId getId() {
        return new EstatisticaAcessoId(igreja.getChave(), data, funcionalidade);
    }

}
