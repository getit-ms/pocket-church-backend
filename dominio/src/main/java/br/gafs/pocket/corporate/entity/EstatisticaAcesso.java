package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.view.View;
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
@Table(name = "tb_estatistica_acesso")
@EqualsAndHashCode(of = {"empresa", "data", "funcionalidade"})
@NamedQueries({
        @NamedQuery(name = "EstatisticaAcesso.findByEmpresaAndFuncionalidade", query = "select ea from EstatisticaAcesso ea where ea.empresa.chave = :empresa and ea.funcionalidade = :funcionalidade order by ea.data"),
        @NamedQuery(name = "EstatisticaAcesso.findOnLine", query = "select new br.gafs.pocket.corporate.entity.EstatisticaAcesso(i, CURRENT_DATE, ra.funcionalidade, (select count(su) from RegistroAcesso su where su.empresa = ra.empresa and su.data between :inicio and :termino and su.funcionalidade = ra.funcionalidade and su.status = :sucesso), (select count(fa) from RegistroAcesso fa where fa.empresa = ra.empresa and fa.data between :inicio and :termino and fa.funcionalidade = ra.funcionalidade and fa.status = :falha)) from RegistroAcesso ra, Empresa i where i.chave = ra.empresa and i.status = :statusEmpresa group by i, ra.funcionalidade "),
        @NamedQuery(name = "EstatisticaAcesso.removeAntigas", query = "delete from EstatisticaAcesso e where e.data < :limite")
})
public class EstatisticaAcesso implements IEntity {
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

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
        return new EstatisticaAcessoId(empresa.getChave(), data, funcionalidade);
    }

}
