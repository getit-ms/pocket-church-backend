/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.dto.FiltroVersiculoDiarioDTO;
import br.gafs.calvinista.dto.FiltroVotacaoDTO;
import br.gafs.calvinista.entity.Opcao;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.VersiculoDiario;
import br.gafs.calvinista.entity.Votacao;
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
@Path("versiculo")
public class VersiculoController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("filtro") @DefaultValue("") String filtro,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.busca(new FiltroVersiculoDiarioDTO(filtro, pagina, total))).build();
    }
    
    @PUT
    @Path("desabilita/{versiculo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desabilita(@PathParam("versiculo") Long versiculo){
        return Response.status(Response.Status.OK).entity(appService.desabilita(versiculo)).build();
    }
    
    @PUT
    @Path("habilita/{versiculo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response habilita(@PathParam("versiculo") Long versiculo){
        return Response.status(Response.Status.OK).entity(appService.habilita(versiculo)).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(VersiculoDiario versiculo){
        appService.cadastra(versiculo);
        return Response.status(Response.Status.OK).build();
    }
    
    @DELETE
    @Path("{versiculo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("versiculo") Long versiculo){
        appService.removeVersiculo(versiculo);
        return Response.status(Response.Status.OK).build();
    }
    
}
