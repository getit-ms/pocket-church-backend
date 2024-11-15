/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusMembro;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.date.DateUtil;
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_membro")
@IdClass(RegistroIgrejaId.class)
@EqualsAndHashCode(of = {"id", "igreja"})
@NamedQueries({
        @NamedQuery(name = "Membro.autentica", query = "select m from Membro m where upper(m.email) = upper(:email) and m.igreja.chave = :igreja and upper(m.senha) = upper(:senha) and m.status in :status"),
        @NamedQuery(name = "Membro.findPastor", query = "select m from Membro m where m.pastor = true and m.igreja.chave = :chaveIgreja and m.status in :status and m.id not in (select ca.pastor.id from CalendarioAtendimento ca where ca.igreja.chave = :chaveIgreja and ca.status = :statusCalendario) order by m.nome"),
        @NamedQuery(name = "Membro.findByEmailIgreja", query = "select m from Membro m where upper(m.email) = upper(:email) and m.igreja.chave = :igreja and m.status in :status"),
        @NamedQuery(name = "Membro.findFuncionalidadesAcessoApp", query = "select f from Membro m inner join m.igreja i inner join i.funcionalidadesAplicativo f where m.id = :membro and i.chave = :igreja group by f"),
        @NamedQuery(name = "Membro.findFuncionalidadesAcessoAdmin", query = "select f from Membro m inner join m.igreja i inner join i.plano p inner join p.funcionalidades f inner join m.acesso a inner join a.perfis pr inner join pr.funcionalidades fu where fu = f and m.id = :membro and i.chave = :igreja group by f")
})
public class Membro implements IEntity {
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_membro")
    @SequenceGenerator(sequenceName = "seq_membro", name = "seq_membro")
    @GeneratedValue(generator = "seq_membro", strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotEmpty
    @Length(max = 150)
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @NotEmpty
    @JsonIgnore
    @Length(max = 250)
    @Column(name = "senha", length = 250, nullable = false)
    private String senha = "undefined";

    @Email
    @Length(max = 150)
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "email", length = 150, nullable = false)
    private String email;

    @JsonView(Detalhado.class)
    @Setter(AccessLevel.NONE)
    @Column(name = "deve_alterar_senha", length = 150, nullable = false)
    private boolean deveAlterarSenha;

    @JsonView(Detalhado.class)
    @Temporal(TemporalType.DATE)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_nascimento")
    private Date dataNascimento;

    @ElementCollection
    @JsonView({Detalhado.class, Aniversariante.class})
    @Column(name = "telefone")
    @View.MergeViews(View.Edicao.class)
    @CollectionTable(name = "rl_telefone_membro",
            joinColumns = {
                    @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
                    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
            })
    private List<String> telefones = new ArrayList<String>();

    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusMembro status = StatusMembro.PENDENTE;

    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "pastor", nullable = false)
    private boolean pastor;

    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "visitante", nullable = false)
    private boolean visitante;

    @JsonView(Detalhado.class)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_endereco", nullable = false)
    private Endereco endereco = new Endereco();

    @JsonView(Detalhado.class)
    @Column(name = "data_exclusao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    @JsonIgnore
    @OneToOne(mappedBy = "membro", cascade = CascadeType.ALL, orphanRemoval = true)
    private Acesso acesso;

    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "dados_disponiveis", nullable = false)
    private boolean dadosDisponiveis = true;

    @Getter(onMethod = @_({
            @JsonProperty,
            @JsonView(Detalhado.class)
    }))
    @Column(name = "deseja_disponibilizar_dados", nullable = false)
    private boolean desejaDisponibilizarDados = true;

    @Setter
    @OneToOne
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_foto", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo foto;

    @Transient
    @View.MergeViews(View.AlterarSenha.class)
    private String novaSenha;

    @Transient
    @View.MergeViews(View.AlterarSenha.class)
    private String confirmacaoSenha;

    public Membro(Igreja igreja) {
        this.igreja = igreja;
    }

    @JsonIgnore
    public void setDesejaDisponibilizarDados(boolean deseja) {
        if (deseja != isDadosDisponiveis()) {
            this.desejaDisponibilizarDados = this.dadosDisponiveis = deseja;
        }
    }

    public void setDadosDisponiveis(boolean dadosDisponiveis) {
        if (this.dadosDisponiveis != dadosDisponiveis) {
            if (dadosDisponiveis && !desejaDisponibilizarDados) {
                throw new ServiceException("mensagens.MSG-044");
            }

            this.dadosDisponiveis = dadosDisponiveis;
        }
    }

    public boolean isDadosDisponiveis() {
        return isDesejaDisponibilizarDados() && dadosDisponiveis;
    }

    @JsonIgnore
    public boolean isSenhaUndefined() {
        return "undefined".equals(senha);
    }

    public boolean isTemAcesso() {
        return acesso != null;
    }

    public void addTelefone(String telefone) {
        telefones.add(telefone);
    }

    public void removeTelefone(String telefone) {
        telefones.remove(telefone);
    }

    public boolean isMembro() {
        return StatusMembro.MEMBRO.equals(status);
    }

    public boolean isAdmin() {
        return isMembro() && acesso != null;
    }

    public void exclui() {
        status = StatusMembro.EXCLUIDO;
        dataExclusao = new Date();
    }

    public boolean isContato() {
        return StatusMembro.CONTATO.equals(status);
    }

    public void contato() {
        if (isMembro()) {
            status = StatusMembro.CONTATO;
            acesso = null;
        }
    }

    public void retiraAdmin() {
        acesso = null;
    }

    public void alteraSenha() {
        if (!StringUtil.isEmpty(novaSenha) &&
                novaSenha.equals(confirmacaoSenha)) {
            senha = SenhaUtil.encryptSHA256(novaSenha);
            deveAlterarSenha = false;
        } else {
            throw new ServiceException("mensagens.MSG-030");
        }
    }

    public boolean membro() {
        if (!isMembro()) {
            visitante = false;
            status = StatusMembro.MEMBRO;

            if ("undefined".equals(senha)) {
                senha = SenhaUtil.geraSenha(8);
                deveAlterarSenha = true;
                return true;
            }
        }
        return false;
    }

    @JsonView(Aniversariante.class)
    public Integer getDiaAniversario() {
        if (getDataNascimento() != null) {
            return Integer.parseInt(DateUtil.formataData(getDataNascimento(), "MMdd"));
        }

        return null;
    }

    public void ativo() {
        status = StatusMembro.CONTATO;
    }

    public interface Aniversariante extends Resumido {
    }
}
