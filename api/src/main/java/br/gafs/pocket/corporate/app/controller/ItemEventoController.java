/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.FiltroComentarioDTO;
import br.gafs.pocket.corporate.dto.FiltroTimelineDTO;
import br.gafs.pocket.corporate.entity.ComentarioItemEvento;
import br.gafs.pocket.corporate.entity.DenunciaComentarioItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Gabriel
 */
@RequestScoped
@Path("item-evento")
public class ItemEventoController {

    @EJB
    private AppService appService;

    @Context
    private HttpServletResponse response;

    @GET
    @Path("timeline")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("autor") final Long autor,
            @QueryParam("semAutor") final Boolean semAutor,
            @QueryParam("tipo") final TipoItemEvento tipo,
            @QueryParam("filtro") final String filtro,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total) {
        return Response.status(Response.Status.OK).entity(appService.buscaTimeline(
                new FiltroTimelineDTO(autor, semAutor != null && semAutor, filtro, pagina, total))).build();
    }

    @GET
    @Path("periodo/{dataInicio}/{dataTermino}")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @PathParam("dataInicio") final String dataInicio,
            @PathParam("dataTermino") final String dataTermino) {
        return Response.status(Response.Status.OK).entity(appService.buscaPeriodoCalendario(
                DateUtil.getDataPrimeiraHoraDia(DateUtil.parseData(dataInicio, "yyyy-MM-dd")),
                DateUtil.getDataUltimaHoraDia(DateUtil.parseData(dataTermino, "yyyy-MM-dd"))
        )).build();
    }

    @POST
    @Path("{tipo}/{id}/curtir")
    public Response curtir(
            @PathParam("tipo") TipoItemEvento tipo,
            @PathParam("id") String id
    ) {
        appService.curteItemEvento(id, tipo);
        return Response.ok().build();
    }

    @DELETE
    @Path("{tipo}/{id}/curtir")
    public Response descurtir(
            @PathParam("tipo") TipoItemEvento tipo,
            @PathParam("id") String id
    ) {
        appService.descurteItemEvento(id, tipo);
        return Response.ok().build();
    }

    @POST
    @Path("{tipo}/{id}/comentario")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response comenta(
            @PathParam("tipo") TipoItemEvento tipo,
            @PathParam("id") String id,
            ComentarioItemEvento comentario
    ) {
        ComentarioItemEvento entidade = new ComentarioItemEvento();
        MergeUtil.merge(comentario, View.Cadastro.class).into(entidade);
        return Response.ok().entity(appService.comenta(id, tipo, entidade)).build();
    }

    @GET
    @Path("{tipo}/{id}/comentario")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaComentarios(
            @PathParam("tipo") TipoItemEvento tipo,
            @PathParam("id") String id,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total) {
        return Response.status(Response.Status.OK).entity(appService.buscaComentarios(
                new FiltroComentarioDTO(id, tipo, pagina, total))).build();
    }

    @DELETE
    @Path("comentario/{id}")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeComentario(
            @PathParam("id") Long id
    ) {
        appService.removeComentario(id);
        return Response.ok().build();
    }

    @POST
    @Path("comentario/{comentario}/denuncia")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response denunciaComentario(
            @PathParam("comentario") Long id,
            DenunciaComentarioItemEvento denuncia
    ) {
        DenunciaComentarioItemEvento entidade = new DenunciaComentarioItemEvento();
        MergeUtil.merge(denuncia, View.Cadastro.class).into(entidade);
        return Response.ok().entity(appService.denunciaComentario(id, entidade)).build();
    }

    @GET
    @Path("denuncia")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaDenuncias() {
        return Response.status(Response.Status.OK).entity(appService.buscaComentarioDenunciados()).build();
    }

    @GET
    @Path("comentario/{comentario}/denuncia")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaDenunciasComentario(
            @PathParam("comentario") Long id
    ) {
        return Response.status(Response.Status.OK).entity(appService.buscaDenunciasComentario(id)).build();
    }

    @POST
    @Path("denuncia/{id}")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atendeDenuncia(
            @PathParam("id") Long id
    ) {
        appService.atendeDenuncia(id);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("denuncia/{id}")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejeitaDenuncia(
            @PathParam("id") Long id
    ) {
        appService.rejeitaDenuncia(id);
        return Response.status(Response.Status.OK).build();
    }
}
