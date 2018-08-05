/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.FiltroAudioDTO;
import br.gafs.pocket.corporate.entity.Audio;
import br.gafs.pocket.corporate.entity.CategoriaAudio;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.view.View;
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
    @JsonView(View.Resumido.class)
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
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaCategorias(){
        return Response.status(Response.Status.OK).entity(appService.buscaCategoriasAudio()).build();
    }

    @GET
    @Path("{audio}")
    @JsonView(View.Detalhado.class)
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