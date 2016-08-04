/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.entity.Institucional;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("institucional")
public class InstituicaoController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(){
        return Response.status(Response.Status.OK).entity(appService.recuperaInstitucional()).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Institucional institucional){
        Institucional entidade = appService.recuperaInstitucional();
        MergeUtil.merge(institucional, View.Edicao.class).into(entidade);
        MergeUtil.merge(institucional.getEndereco(), View.Edicao.class).into(entidade.getEndereco());
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
