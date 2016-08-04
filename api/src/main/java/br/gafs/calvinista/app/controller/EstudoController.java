/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroEstudoDTO;
import br.gafs.calvinista.dto.FiltroEstudoPublicadoDTO;
import br.gafs.calvinista.entity.Estudo;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("estudo")
public class EstudoController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodos(new FiltroEstudoDTO(null, null, pagina, total))).build();
    }

    @GET
    @Path("publicados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPublicados(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaPublicados(new FiltroEstudoPublicadoDTO(pagina, total))).build();
    }
    
    @GET
    @Path("{estudo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("estudo") final Long estudo){
        return Response.status(Response.Status.OK).entity(appService.buscaEstudo(estudo)).build();
    }
    
    @DELETE
    @Path("{estudo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("estudo") final Long estudo){
        appService.removeEstudo(estudo);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Estudo estudo){
        return Response.status(Response.Status.OK).entity(appService.cadastra(estudo)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Estudo estudo){
        Estudo entidade = appService.buscaEstudo(estudo.getId());
        MergeUtil.merge(estudo, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
