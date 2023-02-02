package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusChamado;
import br.gafs.calvinista.entity.domain.TipoChamado;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@ToString(of = "id")
@Table(name = "tb_chamado")
@EqualsAndHashCode(of = "id")
public class Chamado implements IEntity {
    @Id
    @Column(name = "id_chamado")
    @SequenceGenerator(name = "seq_chamado", sequenceName = "seq_chamado")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chamado")
    private Long id;

    @Setter
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_solicitacao", nullable = false)
    private Date dataSolicitacao = new Date();

    @Setter
    @Column(name = "nome_solicitante", nullable = false)
    private String nomeSolicitante;

    @Setter
    @Column(name = "email_solicitante", nullable = false)
    private String emailSolicitante;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_dispositivo_solicitante")
    private Dispositivo dispositivoSolicitante;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_membro_solicitante", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja_solicitante", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Membro membroSolicitante;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja_solicitante")
    private Igreja igrejaSolicitante;

    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusChamado status = StatusChamado.NOVO;

    @Setter
    @Column(name = "tipo")
    @Enumerated(EnumType.ORDINAL)
    private TipoChamado tipo;

    @Column(name = "data_resposta")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataResposta;

    @Column(name = "data_conclusao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConclusao;

    public void aceita() {
        status = StatusChamado.ACEITO;
        dataResposta = new Date();
    }

    public void conclui() {
        status = StatusChamado.CONCLUIDO;
        dataConclusao = new Date();
    }

    public void rejeita() {
        status = StatusChamado.REJEITADO;
        dataResposta = new Date();
        dataConclusao = new Date();
    }

    public boolean isSuporte() {
        return TipoChamado.SUPORTE.equals(tipo);
    }

    public String getCodigo() {
        return Long.toString(id, 36).toUpperCase() +
                DateUtil.formataData(dataSolicitacao, "/MMYY");
    }

    public boolean isConcluido() {
        return dataConclusao != null;
    }

    public void setDispositivoSolicitante(Dispositivo dispositivoSolicitante) {
        this.dispositivoSolicitante = dispositivoSolicitante;
        this.igrejaSolicitante = dispositivoSolicitante.getIgreja();
        this.membroSolicitante = dispositivoSolicitante.getMembro();
        if (this.membroSolicitante != null) {
            this.nomeSolicitante = membroSolicitante.getNome();
            this.emailSolicitante = membroSolicitante.getEmail();
        }
    }
}
