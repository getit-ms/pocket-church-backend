/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.dto.FiltroMensagemDiaDTO;
import br.gafs.pocket.corporate.entity.MensagemDia;
import br.gafs.pocket.corporate.service.AppService;

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
@Path("mensagem-dia")
public class MensagemDiaController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("filtro") @DefaultValue("") String filtro,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.busca(new FiltroMensagemDiaDTO(filtro, pagina, total))).build();
    }
    
    @PUT
    @Path("desabilita/{mensagemDia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desabilita(@PathParam("mensagemDia") Long mensagemDia){
        return Response.status(Response.Status.OK).entity(appService.desabilita(mensagemDia)).build();
    }
    
    @PUT
    @Path("habilita/{mensagemDia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response habilita(@PathParam("mensagemDia") Long mensagemDia){
        return Response.status(Response.Status.OK).entity(appService.habilita(mensagemDia)).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(MensagemDia mensagemDia){
        appService.cadastra(mensagemDia);
        return Response.status(Response.Status.OK).build();
    }
    
    @DELETE
    @Path("{mensagemDia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("mensagemDia") Long mensagemDia){
        appService.removeMensagemDia(mensagemDia);
        return Response.status(Response.Status.OK).build();
    }
    
}
