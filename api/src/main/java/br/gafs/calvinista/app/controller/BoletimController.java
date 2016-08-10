/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroBoletimDTO;
import br.gafs.calvinista.dto.FiltroBoletimPublicadoDTO;
import br.gafs.calvinista.entity.Boletim;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonView;
import java.io.IOException;
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
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("boletim")
public class BoletimController {
    
    @EJB
    private AppService appService;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaTodos(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Status.OK).entity(appService.
                buscaTodos(new FiltroBoletimDTO(null, null, pagina, total))).build();
    }
    
    @GET
    @Path("publicados")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPublicados(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Status.OK).entity(appService.
                buscaPublicados(new FiltroBoletimPublicadoDTO(pagina, total))).build();
    }
    
    @GET
    @Path("{boletim}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("boletim") final Long boletim){
        return Response.status(Status.OK).entity(appService.buscaBoletim(boletim)).build();
    }
    
    @DELETE
    @Path("{boletim}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("boletim") final Long boletim){
        appService.removeBoletim(boletim);
        return Response.status(Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Boletim boletim) throws IOException{
        return Response.status(Status.OK).entity(appService.cadastra(boletim)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Boletim boletim) throws IOException{
        Boletim entidade = appService.buscaBoletim(boletim.getId());
        MergeUtil.merge(boletim, View.Edicao.class).into(entidade);
        return Response.status(Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
