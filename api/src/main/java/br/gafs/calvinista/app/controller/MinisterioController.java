/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("ministerio")
public class MinisterioController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(){
        return Response.status(Response.Status.OK).entity(appService.buscaMinisterios()).build();
    }
    
    @GET
    @Path("acesso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAcessos(){
        return Response.status(Response.Status.OK).entity(appService.buscaMinisteriosPorAcesso()).build();
    }
    
    @GET
    @Path("{ministerio}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("ministerio") final Long ministerio){
        return Response.status(Response.Status.OK).entity(appService.buscaMinisterio(ministerio)).build();
    }
    
    @DELETE
    @Path("{ministerio}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("ministerio") final Long ministerio){
        appService.removeMinisterio(ministerio);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Ministerio ministerio){
        return Response.status(Response.Status.OK).entity(appService.cadastra(ministerio)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Ministerio ministerio){
        Ministerio entidade = appService.buscaMinisterio(ministerio.getId());
        MergeUtil.merge(ministerio, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
