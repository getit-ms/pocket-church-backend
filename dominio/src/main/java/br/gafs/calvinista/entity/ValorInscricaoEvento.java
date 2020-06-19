package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Entity
@ToString(of = "nome")
@EqualsAndHashCode(of = "nome")
@Table(name = "tb_valores_inscricao_evento")
public class ValorInscricaoEvento implements IEntity {
    @Id
    @Column(name = "nome")
    private String nome;

    @Column(name = "valor_texto")
    private String valorTexto;

    @Column(name = "valor_numero")
    private BigDecimal valorNumero;

    @Temporal(TemporalType.DATE)
    @Column(name = "valor_data")
    @View.JsonTemporal(View.JsonTemporalType.DATE)
    private Date valorData;

    @JsonIgnore
    @Column(name = "id_valor_anexo")
    private Long idValorAnexo;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_valor_anexo", referencedColumnName = "id_arquivo", insertable = false, updatable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo valorAnexo;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_inscricao", referencedColumnName = "id_inscricao"),
            @JoinColumn(name = "id_evento", referencedColumnName = "id_evento"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja"),
    })
    private InscricaoEvento inscricao;

    @Override
    @JsonIgnore
    public String getId() {
        return nome;
    }

    public void setValorAnexo(Arquivo valorAnexo) {
        this.valorAnexo = valorAnexo;

        if (valorAnexo != null) {
            this.idValorAnexo = valorAnexo.getId();
        } else {
            this.idValorAnexo = null;
        }
    }
}