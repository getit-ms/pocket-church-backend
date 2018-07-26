/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.ArquivoUtil;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.service.ArquivoService;
import br.gafs.file.EntityFileManager;
import br.gafs.util.string.StringUtil;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("/arquivo")
public class ArquivoController {
    private final static Logger LOGGER = Logger.getLogger(ArquivoController.class.getName());
    private final int chunk_size = 1024 * 1024 * 2; // 2 MB chunks
    
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

    @POST
    @Path("/upload/base64")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadFile(UploadArquivoDTO upload) {
        return Response.status(Status.OK).entity(arquivoService.upload(upload.getFileName(), DatatypeConverter.parseBase64Binary(upload.getData()))).build();
    }

    @GET
    @Path("/download/{arquivo}") 
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response downloadFile(@PathParam("arquivo") Long identificador) throws IOException {
        return downloadFile(identificador, null);
    }

    @GET
    @Path("/download/{arquivo}/{filename}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response downloadFile(
            @PathParam("arquivo") Long identificador,
            @PathParam("filename") String filename) throws IOException {
        Arquivo arquivo = arquivoService.buscaArquivo(identificador);

        if (arquivo != null && (StringUtil.isEmpty(filename) ||
                arquivo.getFilename().equals(filename))){
            File file = EntityFileManager.get(arquivo, "dados");

            response.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("Content-Disposition",
                    "attachment; filename=\""+arquivo.getNome()+"\"");
            ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    @HEAD
    @Path("/stram/{arquivo}/{filename}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response header(
            @PathParam("arquivo") Long identificador,
            @PathParam("filename") String filename) {
        Arquivo arquivo = arquivoService.buscaArquivo(identificador);

        if (arquivo != null && (StringUtil.isEmpty(filename) ||
                arquivo.getFilename().equals(filename))) {
            File file = EntityFileManager.get(arquivo, "dados");

            return Response.ok()
                    .status(Response.Status.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_LENGTH, file.length())
                    .header("Accept-Ranges", "bytes")
                    .build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    @GET
    @Path("/stream/{arquivo}/{filename}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response streamFile(
            @HeaderParam("Range") String range,
            @PathParam("arquivo") Long identificador,
            @PathParam("filename") String filename) throws IOException {
        if (StringUtil.isEmpty(range)) {
            return downloadFile(identificador, filename);
        }


        Arquivo arquivo = arquivoService.buscaArquivo(identificador);

        if (arquivo != null && (StringUtil.isEmpty(filename) ||
                arquivo.getFilename().equals(filename))){
            File file = EntityFileManager.get(arquivo, "dados");

            String[] ranges = range.split( "-" );

            int from = Integer.parseInt( ranges[0] );

            // Chunk media if the range upper bound is unspecified
            int to = chunk_size + from;

            if ( to >= file.length() ) {
                to = (int) ( file.length() - 1 );
            }

            final String responseRange = String.format( "bytes %d-%d/%d", from, to, file.length() );

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.addHeader( "Accept-Ranges", "bytes" );
            response.addHeader( "Content-Range", responseRange );
            response.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("Content-Disposition",
                    "attachment; filename=\""+arquivo.getNome()+"\"");


            ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

            final RandomAccessFile raf = new RandomAccessFile( file, "r" );
            raf.seek( from );

            final int len = to - from + 1;

            ArquivoUtil.transfer(raf, len, response.getOutputStream());

            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }
    
    private byte[] read(InputStream is){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ArquivoUtil.transfer(is, baos);
        return baos.toByteArray();
    }
}
