/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.ArquivoUtil;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ArquivoService;
import br.gafs.calvinista.service.ParametroService;
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
 * @author Gabriel
 */
@RequestScoped
@Path("/arquivo")
public class ArquivoController {
    private final static Logger LOGGER = Logger.getLogger(ArquivoController.class.getName());

    @EJB
    private ArquivoService arquivoService;

    @EJB
    private ParametroService paramService;

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
                arquivo.getFilename().equals(filename))) {
            File file = EntityFileManager.get(arquivo, "dados");

            response.setHeader("Cache-Control", "public, max-age=3600000, post-check=3600000, pre-check=3600000");
            response.setHeader("Last-Modified", "Sun, 06 Nov 2005 15:32:08 GMT");
            response.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + arquivo.getNome() + "\"");
            ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    @HEAD
    @Path("/stream/{arquivo}/{filename}")
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
                    .header("Cache-Control", "public, max-age=3600000, post-check=3600000, pre-check=3600000")
                    .header("Last-Modified", "Sun, 06 Nov 2005 15:32:08 GMT")
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
        boolean validRangeHeader = !StringUtil.isEmpty(range) &&
                range.matches("[Bb][Yy][Tt][Ee][Ss]=\\d+-\\d*");

        if (!validRangeHeader) {
            return downloadFile(identificador, filename);
        }

        Arquivo arquivo = arquivoService.buscaArquivo(identificador);

        if (arquivo != null && (StringUtil.isEmpty(filename) ||
                arquivo.getFilename().equals(filename))) {
            File file = EntityFileManager.get(arquivo, "dados");

            long from = 0, to, chunkSize = paramService.get(arquivo.getChaveIgreja(), TipoParametro.STREAM_CHUNK_SIZE);

            if (validRangeHeader) {
                String[] fromTo = range.split("=")[1].split("-");
                from = Integer.parseInt(fromTo[0]);

                if (fromTo.length > 1) {
                    to = Math.min(Integer.parseInt(fromTo[1]), file.length() - 1);
                } else {
                    to = Math.min((from + chunkSize), file.length()) - 1;
                }
            } else {
                to = Math.min((from + chunkSize), file.length()) - 1;
            }

            final String responseRange = String.format("bytes %d-%d/%d", from, to, file.length());

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.addHeader("Accept-Ranges", "bytes");
            response.addHeader("Content-Range", responseRange);
            response.addHeader("Content-Length", ((to - from) + 1) + "");

            response.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + arquivo.getNome() + "\"");

            ArquivoUtil.transfer(from, to, file, response.getOutputStream(), (int) chunkSize);

            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    private byte[] read(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ArquivoUtil.transfer(is, baos);
        return baos.toByteArray();
    }
}
