/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.entity.Perfil;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
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
    public Response get() {
        return Response.status(Response.Status.OK).entity(appService.buscaPerfis()).build();
    }

    @GET
    @Path("funcionalidades")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFuncionalidades() {
        return Response.status(Response.Status.OK).entity(acessoService.getTodasFuncionalidadesAdmin()).build();
    }

    @GET
    @Path("{perfil}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("perfil") final Long perfil) {
        return Response.status(Response.Status.OK).entity(appService.buscaPerfil(perfil)).build();
    }

    @DELETE
    @Path("{perfil}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("perfil") final Long perfil) {
        appService.removePerfil(perfil);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Perfil perfil) {
        return Response.status(Response.Status.OK).entity(appService.cadastra(perfil)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Perfil perfil) {
        Perfil entidade = appService.buscaPerfil(perfil.getId());
        MergeUtil.merge(perfil, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }

}
