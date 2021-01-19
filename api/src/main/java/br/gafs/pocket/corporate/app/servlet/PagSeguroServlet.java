/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.app.servlet;

import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.sessao.SessionDataManager;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Gabriel
 */
@WebServlet(urlPatterns = "/pagseguro/*")
public class PagSeguroServlet extends HttpServlet {
    
    @EJB
    private AppService appService;
    
    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String empresa = req.getRequestURI().replaceAll("(.+/)?pagseguro/?", "");
        
        String code = req.getParameter("notificationCode");
        String type = req.getParameter("notificationType");
        String transactionId = req.getParameter("transaction_id");
        
        req.setAttribute(SessionDataManager.class.getSimpleName(), new SessionDataManager() {
            @Override
            public String header(String key) {
                if ("Empresa".equals(key)){
                    return empresa;
                }
                return null;
            }

            @Override
            public String parameter(String key) {
                return null;
            }

            @Override
            public void header(String key, String value) {
                
            }
        });
        
        if ("transaction".equals(type)){
            appService.verificaPagSeguroPorCodigo(code);
        }else if (transactionId != null){
            appService.verificaPagSeguroPorIdTransacao(transactionId);
        }
    }
    
}
