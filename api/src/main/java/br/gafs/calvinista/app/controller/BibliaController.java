/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.MyJacksonJsonProvider;
import br.gafs.calvinista.dto.FiltroLivroBibliaDTO;
import br.gafs.calvinista.service.AppService;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;
import java.util.Date;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.fasterxml.jackson.annotation.*;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("biblia")
public class BibliaController {
    
    @EJB
    private AppService appService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("ultimaAtualizacao") String ultimaAtualizacao,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        
        return Response.status(Status.OK).entity(appService.
                busca(new FiltroLivroBibliaDTO(
                        StringUtil.isEmpty(ultimaAtualizacao) ? null : 
                                DateUtil.parseData(ultimaAtualizacao, MyJacksonJsonProvider.DATE_FORMAT), 
                        pagina, total))).build();
    }
    
}
