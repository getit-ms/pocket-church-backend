/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.pocket.corporate.dto.ConfiguracaoCalendarEmpresaDTO;
import br.gafs.pocket.corporate.service.AppService;

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
@Path("calendario")
public class CalendarioController {
    
    @EJB
    private AppService appService;
    
    @Context
    private HttpServletResponse response;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK)
                .entity(appService.buscaEventos(pagina, total)).build();
    }
    
    @GET
    @Path("configuracao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaConfiguracao(){
        return Response.status(Response.Status.OK).entity(appService.buscaConfiguracaoCalendar()).build();
    }

    @GET
    @Path("visoes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaVisoes() throws IOException {
        return Response.status(Response.Status.OK).entity(appService.buscaVisoesCalendar()).build();
    }

    @GET
    @Path("url")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaURL() throws IOException{
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("url", appService.buscaURLAutenticacaoCalendar());
        return Response.status(Response.Status.OK).entity(args).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response desativa() throws IOException{
        appService.desvinculaCalendar();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("integracao")
    public Response redirectConfiguracao(@QueryParam("code") String code, @QueryParam("state") String state) throws IOException{
        response.sendRedirect(MessageFormat.format(ResourceBundleUtil._default().getPropriedade("USER_CALENDAR_REDIRECT_URL"), state, code));
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Path("configuracao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciaConfiguracao(Map<String, String> body){
        appService.iniciaConfiguracaoCalendar(body.get("code"));
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response salva(ConfiguracaoCalendarEmpresaDTO configuracao){
        appService.atualiza(configuracao);
        return Response.status(Response.Status.OK).build();
    }
    
}
