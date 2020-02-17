/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.dto.ContatoDTO;
import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.service.MensagemService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Gabriel
 */
@Path("app")
@RequestScoped
public class ApplicationController {

    @Inject
    private HttpServletRequest request;

    @EJB
    private MensagemService mensagemService;

    @EJB
    private AppService appService;

    @GET
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    public Response version() {
        return Response.status(Response.Status.OK).entity(
                new Versao(request.getServletContext().getInitParameter("projectVersion"))
        ).build();
    }

    @GET
    @Path("version/{tipoDispositivo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response versionApp(@PathParam("tipoDispositivo") TipoDispositivo tipoDispositivo) {
        return Response.status(Response.Status.OK).entity(
                new Versao(appService.getVersaoApp(tipoDispositivo))
        ).build();
    }

    @POST
    @Path("contato")
    @Produces(MediaType.APPLICATION_JSON)
    public Response contato(ContatoDTO contato) {
        mensagemService.enviarMensagem(contato);
        return Response.status(Response.Status.OK).build();
    }

    public class Versao {
        private String version;

        public Versao(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}
