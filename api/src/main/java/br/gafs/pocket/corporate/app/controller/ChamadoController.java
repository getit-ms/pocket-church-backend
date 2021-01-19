/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.dto.FiltroChamadoDTO;
import br.gafs.pocket.corporate.entity.Chamado;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("chamado")
public class ChamadoController {
    
    @EJB
    private AppService appService;

    @GET
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaMeus(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Status.OK).entity(appService.
                busca(new FiltroChamadoDTO(pagina, total))).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Chamado chamado) throws IOException{
        return Response.status(Status.OK).entity(appService.solicita(chamado)).build();
    }

    @GET
    @Path("{chamado}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response carrega(final @PathParam("chamado") Long chamado) throws IOException{
        return Response.status(Status.OK).entity(appService.buscaChamado(chamado)).build();
    }

}
