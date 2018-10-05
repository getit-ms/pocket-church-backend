/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller.infra;

import br.gafs.calvinista.dto.FiltroHinoDTO;
import br.gafs.calvinista.service.AppService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Date;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("infra/hino")
public class HinoController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("filtro") String filtro,
            @QueryParam("ultimaAlteracao") Date ultimaAlteracao,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Status.OK).entity(appService.
                busca(new FiltroHinoDTO(filtro, ultimaAlteracao, pagina, total))).build();
    }
    
    @GET
    @Path("{hino}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("hino") final Long hino){
        return Response.status(Status.OK).entity(appService.buscaHino(hino)).build();
    }
    
}
