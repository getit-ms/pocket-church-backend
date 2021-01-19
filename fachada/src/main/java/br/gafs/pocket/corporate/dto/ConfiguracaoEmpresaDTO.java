/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.view.View;
import br.gafs.dto.DTO;
import br.gafs.util.string.StringUtil;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ConfiguracaoEmpresaDTO implements DTO {
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.USER_PAGSEGURO)
    private String userPagSeguro;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TOKEN_PAGSEGURO)
    private String tokenPagSeguro;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.HABILITADO_PAGSEGURO)
    private boolean habilitadoPagSeguro;
    
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_MENSAGEM_DIA)
    private String tituloMensagemDia;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_ANIVERSARIO)
    private String tituloAniversario;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_ANIVERSARIO)
    private String textoAniversario;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_BOLETIM)
    private String tituloBoletim;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_BOLETIM)
    private String textoBoletim;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_PUBLICACAO)
    private String tituloPublicacao;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_PUBLICACAO)
    private String textoPublicacao;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_DOCUMENTO)
    private String tituloDocumento;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_DOCUMENTO)
    private String textoDocumento;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_NOTICIA)
    private String tituloNoticia;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_NOTICIA)
    private String textoNoticia;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_CLASSIFICADOS)
    private String tituloClassificados;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_CLASSIFICADOS)
    private String textoClassificados;

    public boolean isPagSeguroConfigurado() {
        return !StringUtil.isEmpty(userPagSeguro) &&
                !StringUtil.isEmpty(tokenPagSeguro);
    }

}
