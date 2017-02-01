/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.ArquivoUtil;
import br.gafs.calvinista.dto.FiltroHinoDTO;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.RelatorioService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("hino")
public class HinoController {
    
    @EJB
    private AppService appService;

    @EJB
    private RelatorioService relatorioService;

    @Context
    private HttpServletResponse response;

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
    @Path("{hino}/{tipo}")
    @Produces({"application/pdf", "application/docx", "application/xls", MediaType.APPLICATION_JSON})
    public Response exportaInscricoes(
            @PathParam("hino") Long id,
            @PathParam("tipo") String tipo) throws IOException, InterruptedException {
        File file = relatorioService.exportaHino(id, tipo);

        response.addHeader("Content-Type", "application/" + tipo);
        response.addHeader("Content-Length", "" + file.length());
        response.addHeader("Content-Disposition",
                "attachment; filename=\""+ file.getName() + "\"");
        ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

        return Response.noContent().build();
    }

    @GET
    @Path("{hino}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("hino") final Long hino){
        return Response.status(Status.OK).entity(appService.buscaHino(hino)).build();
    }
    
}
