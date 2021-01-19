/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.ConfiguracaoEmpresaDTO;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.view.View;

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
@Path("configuracao")
public class ConfiguracaoController {
    
    @EJB
    private AppService appService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(){
        return Response.status(Response.Status.OK).entity(appService.buscaConfiguracao()).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(ConfiguracaoEmpresaDTO configuracao){
        ConfiguracaoEmpresaDTO entidade = appService.buscaConfiguracao();
        MergeUtil.merge(configuracao, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
