/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.app;

import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Preferencias;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.sessao.SessionDataManager;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.string.StringUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Gabriel
 */
@WebFilter(urlPatterns = "/rest/*")
public class AutenticacaoFilter implements Filter {
    
    @EJB
    private AcessoService acessoService;
    
    @EJB
    private AppService appService;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse resp = (HttpServletResponse) response;
        
        // Tratamento CORS Domain
        // TODO verificar se é a melhor alternativa
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", req.getHeader("Access-Control-Request-Method"));
        resp.addHeader("Access-Control-Allow-Headers", "igreja, dispositivo, authorization, set-authorization, " + req.getHeader("Access-Control-Request-Headers"));
        resp.addHeader("Access-Control-Expose-Headers", "igreja, dispositivo, authorization, set-authorization");
        
        request.setAttribute(SessionDataManager.class.getSimpleName(), new SessionDataManager() {
            @Override
            public String header(String key) {
                return req.getHeader(key);
            }

            @Override
            public String parameter(String key) {
                return req.getParameter(key);
            }

            @Override
            public void header(String key, String value) {
                resp.setHeader(key, value);
            }
        });
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
    }
}