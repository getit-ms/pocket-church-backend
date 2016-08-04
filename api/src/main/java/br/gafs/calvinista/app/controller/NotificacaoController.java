/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Notificacao;
import br.gafs.calvinista.service.AppService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("notificacao")
public class NotificacaoController {
    
    @EJB
    private AppService appService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response envia(final Notificacao notificacao){
        List<Ministerio> ms = notificacao.getMinisteriosAlvo();
        notificacao.setMinisteriosAlvo(new ArrayList<Ministerio>());
        for (Ministerio m : ms) {
            notificacao.getMinisteriosAlvo().add(appService.buscaMinisterio(m.getId()));
        }
        
        appService.enviar(notificacao);
        return Response.status(Response.Status.OK).build();
    }
    
}
