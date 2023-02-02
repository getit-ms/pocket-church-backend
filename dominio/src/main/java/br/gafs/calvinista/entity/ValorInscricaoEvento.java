package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.FormatoCampoEvento;
import br.gafs.calvinista.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

@Getter
@Entity
@ToString(of = "nome")
@EqualsAndHashCode(of = "nome")
@IdClass(ValorInscricaoEventoId.class)
@Table(name = "tb_valores_inscricao_evento")
public class ValorInscricaoEvento implements IEntity {
    public static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    @Id
    @Column(name = "nome")
    private String nome;

    @Column(name = "formato")
    @Enumerated(EnumType.ORDINAL)
    private FormatoCampoEvento formato;

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

    @Id
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

    public String getValorFormatado() {
        if (valorData != null) {
            return DateUtil.formataData(valorData);
        }

        if (valorNumero != null) {
            if (formato == FormatoCampoEvento.MONETARIO) {
                return CURRENCY_FORMATTER.format(valorNumero);
            }

            return valorNumero.toString();
        }

        if (valorAnexo != null) {
            return valorAnexo.getFilename();
        }

        return valorTexto;
    }
}
