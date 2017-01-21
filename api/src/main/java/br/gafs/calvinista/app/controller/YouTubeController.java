/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.app.util.ReportUtil;
import br.gafs.calvinista.dto.FiltroEventoDTO;
import br.gafs.calvinista.dto.FiltroEventoFuturoDTO;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.dto.FiltroMinhasInscricoesDTO;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.view.View;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.view.relatorio.BuscaPaginadaDataSource;
import net.sf.jasperreports.engine.JRException;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("youtube")
public class YouTubeController {
    
    @EJB
    private AppService appService;
    

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(){
        return Response.status(Response.Status.OK).entity(appService.buscaVideos()).build();
    }
    
    @GET
    @Path("configuracao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaConfiguracao(){
        return Response.status(Response.Status.OK).entity(appService.buscaConfiguracaoYouTube()).build();
    }
    
    @GET
    @Path("url")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaURL(){
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("url", appService.buscaURLAutenticacaoYouTube());
        return Response.status(Response.Status.OK).entity(args).build();
    }
    
    @PUT
    @Path("configuracao/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciaConfiguracao(@PathParam("code") String code){
        appService.iniciaConfiguracaoYouTube(code);
        return Response.status(Response.Status.OK).build();
    }
    
}
