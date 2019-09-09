/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller.infra;

import br.gafs.pocket.corporate.dto.FiltroEmpresaDTO;
import br.gafs.pocket.corporate.service.AppService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("empresa")
public class EmpresaController {

    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("chave") String chave,
            @QueryParam("filtro") String filtro,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("tamanho") @DefaultValue("10") Integer tamanho
    ) {
        return Response.status(Response.Status.OK).entity(appService.busca(
                new FiltroEmpresaDTO(chave, filtro, pagina, tamanho))).build();
    }


    @GET
    @Path("template")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buscaTemplate() {
        return Response.status(Response.Status.OK).entity(appService.buscaTemplate()).build();
    }

    @GET
    @Path("template-app")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buscaTemplateApp() {
        return Response.status(Response.Status.OK).entity(appService.buscaTemplateApp()).build();
    }

}
