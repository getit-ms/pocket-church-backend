/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dto.FiltroFotoDTO;
import br.gafs.calvinista.service.AppService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
@Path("foto")
@RequestScoped
public class FotoController {

    @EJB
    private AppService appService;

    @Context
    private HttpServletResponse response;

    @GET
    @Path("galeria")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaGalerias(
            @QueryParam("pagina") @DefaultValue("1") Integer pagina){
        return Response.status(Status.OK).entity(appService.buscaGaleriasFotos(pagina)).build();
    }

    @GET
    @Path("galeria/{galeria}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaFuturos(
            @PathParam("galeria") String galeria,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina){
        return Response.status(Status.OK).entity(appService.buscaFotos(new FiltroFotoDTO(galeria, pagina))).build();
    }

    @GET
    @Path("url")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaURL() throws IOException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("url", appService.buscaURLAutenticacaoFlickr());
        return Response.status(Response.Status.OK).entity(args).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response desativa() throws IOException{
        appService.desvinculaFlickr();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("integracao/{igreja}")
    public Response redirectConfiguracao(@QueryParam("oauth_verifier") String code, @PathParam("igreja") String igreja) throws IOException{
        response.sendRedirect(MessageFormat.format(ResourceBundleUtil._default().getPropriedade("USER_FLICKR_REDIRECT_URL"), igreja, code));
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("configuracao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciaConfiguracao(Map<String, String> body){
        appService.iniciaConfiguracaoFlickr(body.get("code"));
        return Response.status(Response.Status.OK).build();
    }

}
