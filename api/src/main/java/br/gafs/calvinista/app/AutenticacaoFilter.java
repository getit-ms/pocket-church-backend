/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app;

import br.gafs.calvinista.service.AcessoService;
import br.gafs.util.string.StringUtil;
import java.io.IOException;
import javax.ejb.EJB;
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        
        // Tratamento CORS Domain 
        // TODO verificar se é a melhor alternativa
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", req.getHeader("Access-Control-Request-Method"));
        resp.addHeader("Access-Control-Allow-Headers", "igreja, dispositivo, authorization, " + req.getHeader("Access-Control-Request-Headers"));
        resp.addHeader("Access-Control-Expose-Headers", "igreja, dispositivo, authorization");
        
        if (!"OPTIONS".equals(req.getMethod())){
            String codIgreja = get(req, "Igreja");
            String codDispositivo = get(req, "Dispositivo");
            String chaveAutenticacao = get(req, "Authorization");

            try{
                acessoService.acesso(codIgreja, codDispositivo, chaveAutenticacao);
            }catch(Exception e){
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private String get(HttpServletRequest req, String key){
        String head = req.getHeader(key);
        if (StringUtil.isEmpty(head)){
            return req.getParameter(key);
        }
        return head;
    }
    
    @Override
    public void destroy() {
    }
}
