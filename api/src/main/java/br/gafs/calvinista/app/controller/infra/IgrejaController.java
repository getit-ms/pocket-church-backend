/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller.infra;

import br.gafs.calvinista.dto.FiltroIgrejaDTO;
import br.gafs.calvinista.service.AppService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("igreja")
public class IgrejaController {

    @EJB
    private AppService appService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("chave") String chave,
            @QueryParam("filtro") String filtro,
            @QueryParam("agrupamento") String agrupamento,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("tamanho") @DefaultValue("10") Integer tamanho
    ) {
        return Response.status(Status.OK).entity(appService.busca(
                new FiltroIgrejaDTO(chave, filtro, agrupamento, pagina, tamanho))).build();
    }


    @GET
    @Path("template")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buscaTemplate() {
        return Response.status(Status.OK).entity(appService.buscaTemplate()).build();
    }

    @GET
    @Path("template-app")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buscaTemplateApp() {
        return Response.status(Status.OK).entity(appService.buscaTemplateApp()).build();
    }


}
