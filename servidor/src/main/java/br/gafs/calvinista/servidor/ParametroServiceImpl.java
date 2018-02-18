/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dto.*;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Parametro;
import br.gafs.calvinista.entity.ParametroId;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.entity.domain.TipoParametro.ParametroHandler;
import br.gafs.calvinista.entity.domain.TipoParametro.ParametroSupplier;
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.AllowMembro;
import br.gafs.calvinista.security.AllowUsuario;
import br.gafs.calvinista.security.Audit;
import br.gafs.calvinista.security.AuditoriaInterceptor;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.service.ParametroService;
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
    public ParametrosIgrejaDTO buscaParametros(String igreja) {
        return build(ParametrosIgrejaDTO.class, igreja);
    }

    @Override
    public ConfiguracaoIgrejaDTO buscaConfiguracao(String igreja) {
        return build(ConfiguracaoIgrejaDTO.class, igreja);
    }

    @Audit
    @Override
    @AllowUsuario
    public void salvaParametrosGlobais(ParametrosGlobaisDTO params) {
        extract(params, Parametro.GLOBAL);
    }

    @Override
    public ConfiguracaoCalendarIgrejaDTO buscaConfiguracaoCalendar(String igreja) {
        return build(ConfiguracaoCalendarIgrejaDTO.class, igreja);
    }

    @Override
    public ConfiguracaoYouTubeIgrejaDTO buscaConfiguracaoYouTube(String igreja) {
        return build(ConfiguracaoYouTubeIgrejaDTO.class, igreja);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public void salvaConfiguracaoCalendar(ConfiguracaoCalendarIgrejaDTO params, String igreja) {
        extract(params, igreja);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public void salvaConfiguracaoYouTube(ConfiguracaoYouTubeIgrejaDTO params, String igreja) {
        extract(params, igreja);
    }

    @Audit
    @Override
    @AllowUsuario
    public void salvaParametros(ParametrosIgrejaDTO params, String igreja) {
        extract(params, igreja);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public void salvaConfiguracao(ConfiguracaoIgrejaDTO params, String igreja) {
        extract(params, igreja);
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
