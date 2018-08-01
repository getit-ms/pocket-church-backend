/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.util;

import br.gafs.pocket.corporate.sessao.SessionDataManager;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Gabriel
 */
@RequestScoped
public class CorporateProvider {
    
    @Inject
    private HttpServletRequest request;
    
    @Produces
    public SessionDataManager getSessao(){
        try{
            return (SessionDataManager) request.getAttribute(SessionDataManager.class.getSimpleName());
        }catch(IllegalStateException e){
            return new SessionDataManager(){
                
                @Override
                public String header(String key) {
                    if ("Empresa".equals(key)){
                        return "tst";
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
                
            };
        }
    }
}
