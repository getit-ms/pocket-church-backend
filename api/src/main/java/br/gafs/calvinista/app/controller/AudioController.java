/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroAudioDTO;
import br.gafs.calvinista.entity.Audio;
import br.gafs.calvinista.entity.CategoriaAudio;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("audio")
public class AudioController {
    
    @EJB
    private AppService appService;

    @Context
    private HttpServletResponse response;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("categoria") Long categoria,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodos(
                new FiltroAudioDTO(null, null, categoria, pagina, total))).build();
    }

    @GET
    @Path("categoria")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaCategorias(){
        return Response.status(Response.Status.OK).entity(appService.buscaCategoriasAudio()).build();
    }

    @GET
    @Path("{audio}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("audio") final Long audio){
        return Response.status(Response.Status.OK).entity(appService.buscaAudio(audio)).build();
    }

    @DELETE
    @Path("{audio}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("audio") final Long audio){
        appService.removeAudio(audio);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Audio audio){
        return Response.status(Response.Status.OK).entity(appService.cadastra(audio)).build();
    }

    @POST
    @Path("categoria")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final CategoriaAudio categoria){
        return Response.status(Response.Status.OK).entity(appService.cadastra(categoria)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Audio audio){
        Audio entidade = appService.buscaAudio(audio.getId());
        MergeUtil.merge(audio, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
