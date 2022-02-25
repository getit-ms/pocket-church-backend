/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.geracao.RequisicaoExecucao;
import br.gafs.calvinista.service.GeradorService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("geracao")
public class ExecucaoController {
    private final static Logger LOGGER = Logger.getLogger(ExecucaoController.class.getName());
    
    @EJB
    private GeradorService geradorService;
    
    @Context
    private HttpServletResponse response;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response gera(RequisicaoExecucao req){
        geradorService.schedule(req.getPrioridade(), req);
        return Response.ok().build();
    }
    
    @GET
    @Path("wating")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaWaiting(){
        return Response.ok().entity(geradorService.getPool()).build();
    }
    
    @GET
    @Path("executed")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaExecuted(){
        return Response.ok().entity(geradorService.getFinished()).build();
    }
    
    @GET
    @Path("/download") 
    @Produces({MediaType.APPLICATION_JSON, "application/force-download"})
    public Response downloadFile(@QueryParam("arquivo") String arquivo) throws IOException {
        File file = new File(arquivo);
        if (file.exists() && isAllowed(file)){
            response.addHeader("Content-Type", "application/force-download");
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("Content-Disposition",
                            "attachment; filename=\""+file.getName()+"\"");
            transfer(new FileInputStream(file), response.getOutputStream());
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    private boolean isAllowed(File file){
        return file.getName().endsWith(".war") ||
                file.getName().endsWith(".apk") ||
                file.getName().endsWith(".ipa");
    }
    
    private void transfer(InputStream is, OutputStream os){
        try{
            int size;
            byte[] cache = new byte[5000];
            while ((size = is.read(cache)) > 0){
                os.write(cache, 0, size);
            }

            is.close();
            os.close();
        }catch(Exception e){
            LOGGER.log(Level.SEVERE, "Problema ao transferir dados", e);
        }
    }
}
