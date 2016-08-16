/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroCifraDTO;
import br.gafs.calvinista.entity.Cifra;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
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
@Path("cifra")
public class CifraController {
    
    @EJB
    private AppService appService;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaTodos(
            @QueryParam("filtro") @DefaultValue("") final String filtro,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Status.OK).entity(appService.
                buscaCifras(new FiltroCifraDTO(filtro, pagina, total))).build();
    }
    
    @GET
    @Path("{cifra}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("cifra") final Long cifra){
        return Response.status(Status.OK).entity(appService.buscaCifra(cifra)).build();
    }
    
    @DELETE
    @Path("{cifra}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("cifra") final Long cifra){
        appService.removeCifra(cifra);
        return Response.status(Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Cifra cifra) throws IOException{
        return Response.status(Status.OK).entity(appService.cadastra(cifra)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Cifra cifra) throws IOException{
        Cifra entidade = appService.buscaCifra(cifra.getId());
        MergeUtil.merge(cifra, View.Edicao.class).into(entidade);
        return Response.status(Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
