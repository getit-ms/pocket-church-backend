/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller.infra;

import br.gafs.calvinista.app.controller.*;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.entity.Plano;
import br.gafs.calvinista.entity.Institucional;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.service.AdminService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Arrays;
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
@Path("infra/plano")
public class PlanoController {
    
    @EJB
    private AdminService adminService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(){
        return Response.status(Response.Status.OK).entity(adminService.buscaTodos()).build();
    }
    
    @GET
    @Path("funcionalidades")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFuncionalidades(){
        return Response.status(Response.Status.OK).entity(Arrays.asList(Funcionalidade.values())).build();
    }

    @GET
    @Path("{plano}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("plano") Long plano){
        return Response.status(Response.Status.OK).entity(adminService.buscaPlano(plano)).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadatra(final Plano plano){
        adminService.cadastra(plano);
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Plano plano){
        Plano entidade = adminService.buscaPlano(plano.getId());
        MergeUtil.merge(plano, View.Edicao.class).into(entidade);
        adminService.atualiza(entidade);
        return Response.status(Response.Status.OK).build();
    }
    
    @DELETE
    @Path("{plano}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("plano") Long plano){
        adminService.inativa(plano);
        return Response.status(Response.Status.OK).build();
    }
    
}
