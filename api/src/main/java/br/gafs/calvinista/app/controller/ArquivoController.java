/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.ArquivoService;
import br.gafs.file.EntityFileManager;
import java.io.ByteArrayOutputStream;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("/arquivo")
public class ArquivoController {
    private final static Logger LOGGER = Logger.getLogger(ArquivoController.class.getName());
    
    @EJB
    private ArquivoService arquivoService;
    
    @Context
    private HttpServletResponse response;
    
    @POST
    @Path("/upload") 
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        return Response.status(Status.OK).entity(arquivoService.upload(fileDetail.getFileName(), read(uploadedInputStream))).build();
    }
    
    @GET
    @Path("/download/{arquivo}") 
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response downloadFile(@PathParam("arquivo") Long identificador) throws IOException {
        Arquivo arquivo = arquivoService.buscaArquivo(identificador);
        
        if (arquivo != null){
            File file = EntityFileManager.get(arquivo, "dados");
            
            response.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("Content-Disposition",
                            "attachment; filename=\""+arquivo.getNome()+"\"");
            transfer(new FileInputStream(file), response.getOutputStream());
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }
    
    private byte[] read(InputStream is){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transfer(is, baos);
        return baos.toByteArray();
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
