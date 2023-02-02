/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.dto.FiltroVersiculoDiarioDTO;
import br.gafs.calvinista.entity.VersiculoDiario;
import br.gafs.calvinista.service.AppService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Gabriel
 */
@RequestScoped
@Path("versiculo")
public class VersiculoController {

    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("filtro") @DefaultValue("") String filtro,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total) {
        return Response.status(Response.Status.OK).entity(appService.busca(new FiltroVersiculoDiarioDTO(filtro, pagina, total))).build();
    }

    @PUT
    @Path("desabilita/{versiculo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desabilita(@PathParam("versiculo") Long versiculo) {
        return Response.status(Response.Status.OK).entity(appService.desabilita(versiculo)).build();
    }

    @PUT
    @Path("habilita/{versiculo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response habilita(@PathParam("versiculo") Long versiculo) {
        return Response.status(Response.Status.OK).entity(appService.habilita(versiculo)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(VersiculoDiario versiculo) {
        appService.cadastra(versiculo);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("{versiculo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("versiculo") Long versiculo) {
        appService.removeVersiculo(versiculo);
        return Response.status(Response.Status.OK).build();
    }

}
