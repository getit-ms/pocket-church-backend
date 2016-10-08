/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.dto.FiltroNotificacoesDTO;
import br.gafs.calvinista.dto.QuantidadeDTO;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Notificacao;
import br.gafs.calvinista.service.AppService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response count(){
        return Response.status(Response.Status.OK).entity(new QuantidadeDTO(appService.countNotificacoesNaoLidas())).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.
                buscaNotificacoes(new FiltroNotificacoesDTO(pagina, total))).build();
    }
}
