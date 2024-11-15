/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroNoticiaDTO;
import br.gafs.calvinista.dto.FiltroNoticiaPublicadaDTO;
import br.gafs.calvinista.entity.Noticia;
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
@Path("noticia")
public class NoticiaController {
    
    @EJB
    private AppService appService;

    @Context
    private HttpServletResponse response;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodos(new FiltroNoticiaDTO(null, null, pagina, total))).build();
    }

    @GET
    @Path("publicados")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPublicados(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaPublicados(new FiltroNoticiaPublicadaDTO(pagina, total))).build();
    }
    
    @GET
    @Path("{noticia}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("noticia") final Long noticia){
        return Response.status(Response.Status.OK).entity(appService.buscaNoticia(noticia)).build();
    }

    @DELETE
    @Path("{noticia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("noticia") final Long noticia){
        appService.removeNoticia(noticia);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Noticia noticia){
        return Response.status(Response.Status.OK).entity(appService.cadastra(noticia)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Noticia noticia){
        Noticia entidade = appService.buscaNoticia(noticia.getId());
        MergeUtil.merge(noticia, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
