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
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_membro")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
@NamedQueries({
    @NamedQuery(name = "Membro.autentica", query = "select m from Membro m where upper(m.email) = upper(:email) and m.igreja.chave = :igreja and upper(m.senha) = upper(:senha) and m.status in :status"),
    @NamedQuery(name = "Membro.findPastor", query = "select m from Membro m where m.pastor = true and m.igreja.chave = :chaveIgreja and m.status in :status and m.id not in (select ca.pastor.id from CalendarioAtendimento ca where ca.igreja.chave = :chaveIgreja and ca.status = :statusCalendario) order by m.nome"),
    @NamedQuery(name = "Membro.findByEmailIgreja", query = "select m from Membro m where upper(m.email) = upper(:email) and m.igreja.chave = :igreja and m.status in :status"),
    @NamedQuery(name = "Membro.findFuncionalidadesAcesso", query = "select f from Membro m left join m.acesso a left join a.perfis pr left join pr.funcionalidades fu inner join m.igreja i inner join i.plano p inner join p.funcionalidades f left join i.funcionalidadesAplicativo fa where ((f = fa and f not in :funcionalidadesAdmin) or (pr.id is not null and f = fu)) and i.chave = :igreja and m.id = :membro group by f")
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
    @NotEmpty
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
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_nascimento")
    private Date dataNascimento;
    
    @ElementCollection
    @JsonView(Detalhado.class)
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
    private StatusMembro status = StatusMembro.CONTATO;
    
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

    @Setter
    @JsonView(Detalhado.class)
    @Column(name = "deseja_disponibilizar_dados", nullable = false)
    private boolean desejaDisponibilizarDados = true;

    @Transient
    @View.MergeViews(View.AlterarSenha.class)
    private String novaSenha;
    
    @Transient
    @View.MergeViews(View.AlterarSenha.class)
    private String confirmacaoSenha;
    
    public Membro(Igreja igreja) {
        this.igreja = igreja;
    }
    
    public void setDesejaDisponibilizarDados(boolean deseja){
        if (deseja != isDadosDisponiveis()){
            this.desejaDisponibilizarDados = this.dadosDisponiveis = deseja;
        }
    }
    
    public void setDadosDisponiveis(boolean dadosDisponiveis){
        if (isDadosDisponiveis() != dadosDisponiveis){
            if (dadosDisponiveis && !desejaDisponibilizarDados){
                throw new ServiceException("mensagens.MSG-");
            }
        
            this.dadosDisponiveis = dadosDisponiveis;
        }
    }
    
    public boolean isDadosDisponiveis(){
        return isDesejaDisponibilizarDados() && dadosDisponiveis;
    }
    
    @JsonIgnore
    public boolean isSenhaUndefined(){
        return "undefined".equals(senha);
    }
    
    public boolean isTemAcesso(){
        return acesso != null;
    }
    
    public void addTelefone(String telefone){
        telefones.add(telefone);
    }
    
    public void removeTelefone(String telefone){
        telefones.remove(telefone);
    }
    
    public boolean isMembro(){
        return StatusMembro.MEMBRO.equals(status);
    }
    
    public boolean isAdmin(){
        return isMembro() && acesso != null;
    }
    
    public void exclui(){
        status = StatusMembro.EXCLUIDO;
        dataExclusao = new Date();
    }
    
    public boolean isContato(){
        return StatusMembro.CONTATO.equals(status);
    }
    
    public void contato(){
        if (isMembro()){
            status = StatusMembro.CONTATO;
            acesso = null;
        }
    }
    
    public void retiraAdmin(){
        acesso = null;
    }
    
    public void alteraSenha(){
        if (!StringUtil.isEmpty(novaSenha) &&
                novaSenha.equals(confirmacaoSenha)){
            senha = SenhaUtil.encryptSHA256(novaSenha);
            deveAlterarSenha = false;
        }else{
            throw new ServiceException("mensagens.MSG-030");
        }
    }
    
    public boolean membro(){
        if (!isMembro()){
            visitante = false;
            status = StatusMembro.MEMBRO;
            
            if ("undefined".equals(senha)){
                senha = SenhaUtil.geraSenha(8);
                deveAlterarSenha = true;
                return true;
            }
        }
        return false;
    }
}
