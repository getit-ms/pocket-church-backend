/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dto.ConfiguracaoFacebookIgrejaDTO;
import br.gafs.calvinista.service.AppService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("facebook/video")
public class VideoFacebookController {
    
    @EJB
    private AppService appService;
    
    @Context
    private HttpServletResponse response;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(){
        return Response.status(Response.Status.OK).entity(appService.buscaVideosFacebook()).build();
    }
    
    @GET
    @Path("configuracao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaConfiguracao(){
        return Response.status(Response.Status.OK).entity(appService.buscaConfiguracaoVideosFacebook()).build();
    }

    @GET
    @Path("paginas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPaginas() throws IOException {
        return Response.status(Response.Status.OK).entity(appService.buscaPaginasFacebook()).build();
    }

    @GET
    @Path("url")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaURL() throws IOException{
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("url", appService.buscaURLAutenticacaoVideosFacebook());
        return Response.status(Response.Status.OK).entity(args).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response desativa() throws IOException{
        appService.desvinculaVideosFacebook();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("integracao")
    public Response redirectConfiguracao(@QueryParam("code") String code,
                                         @QueryParam("state") String chave) throws IOException{
        response.sendRedirect(MessageFormat.format(ResourceBundleUtil._default().getPropriedade("USER_FACEBOOK_VIDEO_REDIRECT_URL"), chave, code));
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Path("configuracao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciaConfiguracao(Map<String, String> body){
        appService.iniciaConfiguracaoVideosFacebook(body.get("code"));
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response salva(ConfiguracaoFacebookIgrejaDTO configuracao){
        appService.atualiza(configuracao);
        return Response.status(Response.Status.OK).build();
    }
    
}
