/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.entity.domain.Funcionalidade;
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
@Path("aplicativo")
public class FuncionalidadeAplicativoController {

    @EJB
    private AppService appService;

    @GET
    @Path("funcionalidades")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaFuncionalidadesHabilitadas() {
        return Response.status(Response.Status.OK).entity(appService.getFuncionalidadesHabilitadasAplicativo()).build();
    }

    @GET
    @Path("funcionalidades/todas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaFuncionalidades() {
        return Response.status(Response.Status.OK).entity(appService.getFuncionalidadesAplicativo()).build();
    }

    @PUT
    @Path("funcionalidades")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response salvaFuncionalidadesHabilitadas(List<Funcionalidade> funcionalidades) {
        appService.salvaFuncionalidadesHabilitadasAplicativo(funcionalidades);
        return Response.status(Response.Status.OK).build();
    }

}
