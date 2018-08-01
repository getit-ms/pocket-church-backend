/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.servidor;

import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.entity.Parametro;
import br.gafs.pocket.corporate.entity.ParametroId;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.entity.domain.TipoParametro.ParametroHandler;
import br.gafs.pocket.corporate.entity.domain.TipoParametro.ParametroSupplier;
import br.gafs.pocket.corporate.security.AllowAdmin;
import br.gafs.pocket.corporate.security.AllowUsuario;
import br.gafs.pocket.corporate.security.Audit;
import br.gafs.pocket.corporate.security.AuditoriaInterceptor;
import br.gafs.pocket.corporate.security.SecurityInterceptor;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.dao.DAOService;
import br.gafs.logger.ServiceLoggerInterceptor;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

/**
 *
 * @author Gabriel
 */
@Stateless
@Local(ParametroService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class, SecurityInterceptor.class})
public class ParametroServiceImpl implements ParametroService {
    
    @EJB
    private DAOService daoService;

    @Override
    public ParametrosGlobaisDTO buscaParametrosGlobais() {
        return build(ParametrosGlobaisDTO.class, Parametro.GLOBAL);
    }

    @Override
    public ParametrosEmpresaDTO buscaParametros(String empresa) {
        return build(ParametrosEmpresaDTO.class, empresa);
    }

    @Override
    public ConfiguracaoEmpresaDTO buscaConfiguracao(String empresa) {
        return build(ConfiguracaoEmpresaDTO.class, empresa);
    }

    @Audit
    @Override
    @AllowUsuario
    public void salvaParametrosGlobais(ParametrosGlobaisDTO params) {
        extract(params, Parametro.GLOBAL);
    }

    @Override
    public ConfiguracaoCalendarEmpresaDTO buscaConfiguracaoCalendar(String empresa) {
        return build(ConfiguracaoCalendarEmpresaDTO.class, empresa);
    }

    @Override
    public ConfiguracaoYouTubeEmpresaDTO buscaConfiguracaoYouTube(String empresa) {
        return build(ConfiguracaoYouTubeEmpresaDTO.class, empresa);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public void salvaConfiguracaoCalendar(ConfiguracaoCalendarEmpresaDTO params, String empresa) {
        extract(params, empresa);
    }

    @Override
    public ConfiguracaoFlickrEmpresaDTO buscaConfiguracaoFlickr(String empresa) {
        return build(ConfiguracaoFlickrEmpresaDTO.class, empresa);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public void salvaConfiguracaoYouTube(ConfiguracaoYouTubeEmpresaDTO params, String empresa) {
        extract(params, empresa);
    }

    @Audit
    @Override
    @AllowUsuario
    public void salvaParametros(ParametrosEmpresaDTO params, String empresa) {
        extract(params, empresa);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public void salvaConfiguracao(ConfiguracaoEmpresaDTO params, String empresa) {
        extract(params, empresa);
    }
    
    private void extract(Object obj, final String grupo){
        TipoParametro.extract(obj, new ParametroSupplier(){
            @Override
            public Parametro get(TipoParametro tipo) {
                Parametro param = daoService.find(Parametro.class, new ParametroId(grupo, tipo));
                if (param == null){
                    return new Parametro(grupo, tipo);
                }
                return param;
            }
        }, new ParametroHandler(){
            @Override
            public void handle(Parametro param) {
                daoService.update(param);
            }
        });
    }
    
    private <T> T build(Class<T> type, final String grupo){
        return TipoParametro.build(type, new ParametroSupplier(){
            @Override
            public Parametro get(TipoParametro tipo) {
                Parametro param = daoService.find(Parametro.class, new ParametroId(grupo, tipo));
                if (param == null){
                    return new Parametro(grupo, tipo);
                }
                return param;
            }
        });
    }

    @Override
    public <T> T get(String grupo, TipoParametro param) {
        Parametro p = daoService.find(Parametro.class, new ParametroId(grupo, param));
        
        if (p == null){
            p = new Parametro(grupo, param);
        }
        
        return p.get();
    }

    @Override
    public <T> void set(String grupo, TipoParametro param, T value) {
        Parametro p = new Parametro(grupo, param);
        p.set(value);
        daoService.update(p);
    }
    
}
