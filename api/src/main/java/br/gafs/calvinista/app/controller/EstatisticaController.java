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
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@Path("estatistica")
@RequestScoped
public class EstatisticaController {
    
    @EJB
    private AppService appService;
    
    @Context
    private HttpServletResponse response;
    
    @GET
    @Path("dispositivos/quantidade")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaQuantidadeDispositivos(){
        return Response.status(Response.Status.OK).entity(buscaQuantidadeDispositivos()).build();
    }

    @GET
    @Path("dispositivos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaEstatisticasDispositivos(){
        return Response.status(Response.Status.OK).entity(appService.buscaEstatisticasDispositivos()).build();
    }

    @GET
    @Path("acessos/{funcionalidade}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaEstatisticasAcesso(final @PathParam("funcionalidade") String funcionalidade){
        return Response.ok().entity(appService.buscaEstatisticasAcessoFuncionalidade(Funcionalidade.valueOf(funcionalidade))).build();
    }
    
}
