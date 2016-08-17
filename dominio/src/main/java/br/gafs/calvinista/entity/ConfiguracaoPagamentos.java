package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Entity
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_configuracao_pagamentos")
public class ConfiguracaoPagamentos implements IEntity {
    @Id
    @Setter
    @OneToOne
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Setter
    @Column(name = "user_pagseguro")
    @View.MergeViews(View.Edicao.class)
    private String userPagSeguro;

    @Setter
    @Column(name = "token_pagseguro")
    @View.MergeViews(View.Edicao.class)
    private String tokenPagSeguro;

    @Setter
    @Column(name = "habilitado_pagseguro")
    @View.MergeViews(View.Edicao.class)
    private boolean habilitadoPagSeguro;

    @JsonIgnore
    public boolean isPagSeguroConfigurado(){
        return !StringUtil.isEmpty(userPagSeguro) &&
                !StringUtil.isEmpty(tokenPagSeguro);
    }

    public boolean isHabilitadoPagSeguro(){
        return isPagSeguroConfigurado() &&
                habilitadoPagSeguro;
    }

    @Override
    public Igreja getId() {
        return igreja;
    }
}
