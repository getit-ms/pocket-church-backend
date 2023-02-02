/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.util;

import br.gafs.calvinista.sessao.SessionDataManager;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Gabriel
 */
@RequestScoped
public class CalvinProvider {

    @Inject
    private HttpServletRequest request;

    @Produces
    public SessionDataManager getSessao() {
        try {
            return (SessionDataManager) request.getAttribute(SessionDataManager.class.getSimpleName());
        } catch (IllegalStateException e) {
            return new SessionDataManager() {

                @Override
                public String header(String key) {
                    if ("Igreja".equals(key)) {
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
