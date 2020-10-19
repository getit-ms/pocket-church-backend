/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.dto.FiltroTimelineDTO;
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
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total) {
        return Response.status(Response.Status.OK).entity(appService.buscaTimeline(
                new FiltroTimelineDTO(pagina, total))).build();
    }

    @GET
    @Path("periodo/{dataInicio}/{dataTermino}")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @PathParam("dataInicio") final String dataInicio,
            @PathParam("dataTermino") final String dataTermino) {
        return Response.status(Response.Status.OK).entity(appService.buscaPeriodoCalendario(
                DateUtil.parseData(dataInicio, "yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateUtil.parseData(dataTermino, "yyyy-MM-dd'T'HH:mm:ss.SSS")
        )).build();
    }

}
