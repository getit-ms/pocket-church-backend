/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.app;

import br.gafs.calvinista.sessao.SessionDataManager;
import java.io.IOException;
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
@WebFilter(urlPatterns = {"/rest/*","/ajuda/*"})
public class AutenticacaoFilter implements Filter {
    
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
        resp.addHeader("Access-Control-Expose-Headers", "igreja, dispositivo, authorization, set-authorization, force-register, force-reset");
        
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