/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.dto.FiltroHinoDTO;
import br.gafs.calvinista.service.AppService;
import java.util.Date;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("hino")
public class HinoController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("filtro") String filtro,
            @QueryParam("ultimaAtualizacao") Date ultimaAtualizacao,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Status.OK).entity(appService.
                busca(new FiltroHinoDTO(filtro, ultimaAtualizacao, pagina, total))).build();
    }
    
    @GET
    @Path("{hino}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("hino") final Long hino){
        return Response.status(Status.OK).entity(appService.buscaHino(hino)).build();
    }
    
}
