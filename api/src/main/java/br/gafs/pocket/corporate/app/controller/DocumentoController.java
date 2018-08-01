/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.ArquivoUtil;
import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.FiltroDocumentoDTO;
import br.gafs.pocket.corporate.dto.FiltroDocumentoPublicadoDTO;
import br.gafs.pocket.corporate.entity.CategoriaDocumento;
import br.gafs.pocket.corporate.entity.Documento;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.service.RelatorioService;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("documento")
public class DocumentoController {
    
    @EJB
    private AppService appService;

    @EJB
    private RelatorioService relatorioService;

    @Context
    private HttpServletResponse response;

    @GET
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodos(new FiltroDocumentoDTO(null, null, null, pagina, total))).build();
    }

    @GET
    @Path("categoria")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaCategorias(){
        return Response.status(Response.Status.OK).entity(appService.buscaCategoriasDocumento()).build();
    }

    @GET
    @Path("publicados")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPublicados(
            @QueryParam("categoria") final Long categoria,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaPublicados(new FiltroDocumentoPublicadoDTO(categoria, pagina, total))).build();
    }
    
    @GET
    @Path("{documento}")
    @JsonView(View.Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("documento") final Long documento){
        return Response.status(Response.Status.OK).entity(appService.buscaDocumento(documento)).build();
    }

    @GET
    @Path("{documento}/{nome}.{tipo}")
    @Produces({"application/pdf", "application/docx", "application/xls", MediaType.APPLICATION_JSON})
    public Response exportaInscricoes(
            @PathParam("documento") Long id,
            @PathParam("tipo") String tipo,
            @PathParam("nome") String nome) throws IOException, InterruptedException {
        File file = relatorioService.exportaDocumento(id, tipo);

        response.addHeader("Content-Type", "application/" + tipo);
        response.addHeader("Content-Length", "" + file.length());
        response.addHeader("Content-Disposition",
                "attachment; filename=\""+ file.getName() + "\"");
        ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

        return Response.noContent().build();
    }
    
    @DELETE
    @Path("{documento}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("documento") final Long documento){
        appService.removeDocumento(documento);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Documento documento){
        return Response.status(Response.Status.OK).entity(appService.cadastra(documento)).build();
    }

    @POST
    @Path("categoria")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final CategoriaDocumento categoria){
        return Response.status(Response.Status.OK).entity(appService.cadastra(categoria)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Documento documento){
        Documento entidade = appService.buscaDocumento(documento.getId());
        MergeUtil.merge(documento, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
