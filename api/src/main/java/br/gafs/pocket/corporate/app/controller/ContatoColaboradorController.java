/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.dto.DateParam;
import br.gafs.pocket.corporate.dto.FiltroMeusContatosColaboradorDTO;
import br.gafs.pocket.corporate.dto.FiltroContatoColaboradorDTO;
import br.gafs.pocket.corporate.entity.ContatoColaborador;
import br.gafs.pocket.corporate.entity.domain.StatusContatoColaborador;
import br.gafs.pocket.corporate.service.AppService;

import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
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
@Path("contato-colaborador")
public class ContatoColaboradorController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("dataInicio") DateParam dataInicio, 
            @QueryParam("dataTermino") DateParam dataTermino,
            @QueryParam("status") List<StatusContatoColaborador> status,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodos(new FiltroContatoColaboradorDTO(
                        dataInicio != null ? dataInicio.getData() : null,
                        dataTermino != null ? dataTermino.getData() : null, 
                        status, pagina, total))).build();
    }
    
    @GET
    @Path("meus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMeus(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaMeus(new FiltroMeusContatosColaboradorDTO(pagina, total))).build();
    }
    
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus(){
        return Response.status(Response.Status.OK).entity(StatusContatoColaborador.values()).build();
    }
    
    @PUT
    @Path("atende/{idContato}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response atende(@PathParam("idContato") Long id){
        appService.atende(id);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submete(ContatoColaborador contato){
        return Response.status(Response.Status.OK).entity(appService.realizaContato(contato)).build();
    }
    
}
