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
    @TipoParametro.Mapping(TipoParametro.TITULO_ANIVERSARIO)
    private String tituloAniversario;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_ANIVERSARIO)
    private String textoAniversario;
    @TipoParametro.Mapping(TipoParametro.TITULO_BOLETIM)
    private String tituloBoletim;
    @View.MergeViews(View.Edicao.class)
    @TipoParametro.Mapping(TipoParametro.TEXTO_BOLETIM)
    private String textoBoletim;

    public boolean isPagSeguroConfigurado() {
        return !StringUtil.isEmpty(userPagSeguro) &&
                !StringUtil.isEmpty(tokenPagSeguro);
    }

}
