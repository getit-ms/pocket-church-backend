/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.entity.Banner;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("banner")
public class BannerController {
    
    @EJB
    private AppService appService;

    @GET
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaTodos(){
        return Response.status(Status.OK).entity(appService.buscaBanners()).build();
    }
    
    @GET
    @Path("{banner}")
    @JsonView(View.Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("banner") final Long banner){
        return Response.status(Status.OK).entity(appService.buscaBanner(banner)).build();
    }
    
    @DELETE
    @Path("{banner}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("banner") final Long banner){
        appService.removeBanner(banner);
        return Response.status(Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Banner banner) throws IOException{
        return Response.status(Status.OK).entity(appService.cadastra(banner)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Banner banner) throws IOException{
        Banner entidade = appService.buscaBanner(banner.getId());
        MergeUtil.merge(banner, View.Edicao.class).into(entidade);
        return Response.status(Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
