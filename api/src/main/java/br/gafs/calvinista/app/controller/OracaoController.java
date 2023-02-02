/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.dto.DateParam;
import br.gafs.calvinista.dto.FiltroMeusPedidoOracaoDTO;
import br.gafs.calvinista.dto.FiltroPedidoOracaoDTO;
import br.gafs.calvinista.entity.PedidoOracao;
import br.gafs.calvinista.entity.domain.StatusPedidoOracao;
import br.gafs.calvinista.service.AppService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


/**
 * @author Gabriel
 */
@RequestScoped
@Path("oracao")
public class OracaoController {

    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("dataInicio") DateParam dataInicio,
            @QueryParam("dataTermino") DateParam dataTermino,
            @QueryParam("status") List<StatusPedidoOracao> status,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total) {
        return Response.status(Response.Status.OK).entity(appService.buscaTodos(new FiltroPedidoOracaoDTO(
                dataInicio != null ? dataInicio.getData() : null,
                dataTermino != null ? dataTermino.getData() : null,
                status, pagina, total))).build();
    }

    @GET
    @Path("meus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMeus(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total) {
        return Response.status(Response.Status.OK).entity(appService.buscaMeus(new FiltroMeusPedidoOracaoDTO(pagina, total))).build();
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        return Response.status(Response.Status.OK).entity(StatusPedidoOracao.values()).build();
    }

    @PUT
    @Path("atende/{idPedido}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response atende(@PathParam("idPedido") Long id) {
        appService.atende(id);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submete(PedidoOracao pedido) {
        return Response.status(Response.Status.OK).entity(appService.realizaPedido(pedido)).build();
    }

}
