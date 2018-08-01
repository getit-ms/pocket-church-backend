/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.entity.Perfil;
import br.gafs.pocket.corporate.service.AcessoService;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.view.View;

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
@Path("perfil")
public class PerfilController {
    
    @EJB
    private AppService appService;
    
    @EJB
    private AcessoService acessoService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(){
        return Response.status(Response.Status.OK).entity(appService.buscaPerfis()).build();
    }
    
    @GET
    @Path("funcionalidades")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFuncionalidades(){
        return Response.status(Response.Status.OK).entity(acessoService.getTodasFuncionalidadesAdmin()).build();
    }
    
    @GET
    @Path("{perfil}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("perfil") final Long perfil){
        return Response.status(Response.Status.OK).entity(appService.buscaPerfil(perfil)).build();
    }
    
    @DELETE
    @Path("{perfil}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("perfil") final Long perfil){
        appService.removePerfil(perfil);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Perfil perfil){
        return Response.status(Response.Status.OK).entity(appService.cadastra(perfil)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Perfil perfil){
        Perfil entidade = appService.buscaPerfil(perfil.getId());
        MergeUtil.merge(perfil, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
