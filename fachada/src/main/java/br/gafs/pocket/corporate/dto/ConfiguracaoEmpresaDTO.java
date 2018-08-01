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
    @TipoParametro.Mapping(TipoParametro.TITULO_MENSAGEM_DIA)
    private String tituloMensagemDia;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TITULO_ANIVERSARIO)
    private String tituloAniversario;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_ANIVERSARIO)
    private String textoAniversario;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TITULO_BOLETIM)
    private String tituloBoletim;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_BOLETIM)
    private String textoBoletim;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TITULO_PUBLICACAO)
    private String tituloPublicacao;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_PUBLICACAO)
    private String textoPublicacao;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TITULO_DOCUMENTO)
    private String tituloDocumento;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_DOCUMENTO)
    private String textoDocumento;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TITULO_NOTICIA)
    private String tituloNoticia;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_NOTICIA)
    private String textoNoticia;

    public boolean isPagSeguroConfigurado() {
        return !StringUtil.isEmpty(userPagSeguro) &&
                !StringUtil.isEmpty(tokenPagSeguro);
    }

}
