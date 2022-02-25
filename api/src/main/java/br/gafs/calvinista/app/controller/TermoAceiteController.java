/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.entity.TermoAceite;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
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
@Path("termo-aceite")
public class TermoAceiteController {

    @EJB
    private AppService appService;

    @GET
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response detalha() {
        return Response.status(Status.OK).entity(appService.buscaUltimoTermo()).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final TermoAceite termoAceite) throws IOException {
        return Response.status(Status.OK).entity(appService.cadastra(termoAceite)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final TermoAceite termoAceite) throws IOException {
        TermoAceite entidade = appService.buscaUltimoTermo();
        MergeUtil.merge(termoAceite, View.Edicao.class).into(entidade);
        return Response.status(Status.OK).entity(appService.atualiza(entidade)).build();
    }

    @POST
    @Path("aceite")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aceitaTermo() throws IOException {
        appService.aceitaTermo();
        return Response.status(Status.OK).build();
    }
}
