/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.MyJacksonJsonProvider;
import br.gafs.calvinista.dto.FiltroDevocionarioDTO;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroDevocionarioPublicadoDTO;
import br.gafs.calvinista.entity.DiaDevocionario;
import br.gafs.calvinista.entity.domain.StatusDiaDevocionario;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

/**
 * @author Gabriel
 */
@RequestScoped
@Path("devocionario")
public class DevocionarioController {

    @EJB
    private AppService appService;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaTodos(
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataTermino") String dataTermino,
            @QueryParam("status") StatusDiaDevocionario status,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total) {
        return Response.status(Status.OK).entity(appService.
                buscaTodos(new FiltroDevocionarioDTO(
                        StringUtil.isEmpty(dataInicio) ? null :
                                DateUtil.parseData(dataInicio, MyJacksonJsonProvider.DATE_FORMAT),
                        StringUtil.isEmpty(dataTermino) ? null :
                                DateUtil.parseData(dataTermino, MyJacksonJsonProvider.DATE_FORMAT),
                        status, pagina, total))).build();
    }

    @GET
    @Path("publicados")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPublicados(
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataTermino") String dataTermino,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total) {
        return Response.status(Status.OK).entity(appService.
                buscaPublicados(new FiltroDevocionarioPublicadoDTO(
                        StringUtil.isEmpty(dataInicio) ? null :
                                DateUtil.parseData(dataInicio, MyJacksonJsonProvider.DATE_FORMAT),
                        StringUtil.isEmpty(dataTermino) ? null :
                                DateUtil.parseData(dataTermino, MyJacksonJsonProvider.DATE_FORMAT),
                        pagina, total))).build();
    }

    @DELETE
    @Path("{dia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("dia") final Long dia) {
        appService.removeDiaDevocionario(dia);
        return Response.status(Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final DiaDevocionario diaDevocionario) throws IOException {
        return Response.status(Status.OK).entity(appService.cadastra(diaDevocionario)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final DiaDevocionario diaDevocionario) throws IOException {
        DiaDevocionario entidade = appService.buscaDiaDevocionario(diaDevocionario.getId());
        MergeUtil.merge(diaDevocionario, View.Edicao.class).into(entidade);
        return Response.status(Status.OK).entity(appService.atualiza(entidade)).build();
    }

}
