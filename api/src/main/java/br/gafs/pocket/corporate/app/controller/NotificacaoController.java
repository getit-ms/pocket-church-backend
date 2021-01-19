/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.dto.FiltroNotificacoesDTO;
import br.gafs.pocket.corporate.dto.QuantidadeDTO;
import br.gafs.pocket.corporate.entity.Notificacao;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.service.MensagemService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("notificacao")
public class NotificacaoController {
    
    @EJB
    private AppService appService;

    @EJB
    private MensagemService mensagemService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response envia(final Notificacao notificacao){
        appService.enviar(notificacao);
        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response count(){
        return Response.status(Response.Status.OK)
                .entity(new QuantidadeDTO(mensagemService.countNotificacoesNaoLidas())).build();
    }

    @DELETE
    @Path("clear")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clear(@QueryParam("excecao") List<Long> excecoes){
        appService.clearNotificacoes(excecoes);
        return Response.status(Response.Status.OK).build();
    }
    
    @DELETE
    @Path("{notificacao:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("notificacao") Long notificacao){
        appService.removeNotificacao(notificacao);
        return Response.status(Response.Status.OK).build();
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
