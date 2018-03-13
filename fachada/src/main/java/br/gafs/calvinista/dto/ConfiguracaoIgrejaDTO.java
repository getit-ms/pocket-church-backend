/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.view.View;
import br.gafs.dto.DTO;
import br.gafs.util.string.StringUtil;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ConfiguracaoIgrejaDTO implements DTO {
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
    @TipoParametro.Mapping(TipoParametro.TITULO_VERSICULO_DIARIO)
    private String tituloVersiculoDiario;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TITULO_LEMBRETE_LEITURA_BIBLICA)
    private String tituloLembreteLeituraBiblica;
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
    @TipoParametro.Mapping(TipoParametro.TITULO_ESTUDO)
    private String tituloEstudo;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_ESTUDO)
    private String textoEstudo;
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
