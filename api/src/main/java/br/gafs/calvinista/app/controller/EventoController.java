/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.ArquivoUtil;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroEventoDTO;
import br.gafs.calvinista.dto.FiltroEventoFuturoDTO;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.dto.FiltroMinhasInscricoesDTO;
import br.gafs.calvinista.entity.CampoEvento;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.ValorInscricaoEvento;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.RelatorioService;
import br.gafs.calvinista.view.View;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.string.StringUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("evento")
public class EventoController {
    
    @EJB
    private AppService appService;

    @EJB
    private RelatorioService relatorioService;

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(
            @QueryParam("tipo") @DefaultValue("EVENTO") TipoEvento tipo,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodos(new FiltroEventoDTO(null, null, tipo, pagina, total))).build();
    }
    
    @GET
    @Path("proximos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaFuturos(
            @QueryParam("tipo") @DefaultValue("EVENTO") TipoEvento tipo,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaFuturos(new FiltroEventoFuturoDTO(tipo, pagina, total))).build();
    }
    
    @GET
    @Path("{evento}/inscricoes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaInscricoes(
            @PathParam("evento") Long evento, 
            @QueryParam("pagina") @DefaultValue("1") Integer pagina, 
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodas(evento,
                new FiltroInscricaoDTO(null, null, pagina, total))).build();
    }

    @GET
    @Path("{evento}/inscricoes/{filename}.{tipo}")
    @Produces({"application/pdf", "application/docx", "application/xls", MediaType.APPLICATION_JSON})
    public Response exportaInscricoes(
            @PathParam("evento") Long id,
            @PathParam("tipo") String tipo,
            @PathParam("filename") String filename) throws IOException, InterruptedException {
        File file = relatorioService.exportaInscritos(id, tipo);

        response.addHeader("Content-Type", "application/" + tipo);
        response.addHeader("Content-Length", "" + file.length());
        response.addHeader("Content-Disposition",
                "attachment; filename=\""+ file.getName() + "\"");
        ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

        return Response.noContent().build();
    }

    @GET
    @Path("inscricoes/{tipo}.xls")
    @Produces({"application/xls", MediaType.APPLICATION_JSON})
    public Response exportaInscricoes(@PathParam("tipo") TipoEvento tipo) throws IOException, InterruptedException {
        File file = relatorioService.exportaInscritos(tipo);

        response.addHeader("Content-Type", "application/xls");
        response.addHeader("Content-Length", "" + file.length());
        response.addHeader("Content-Disposition",
                "attachment; filename=\""+ file.getName() + "\"");
        ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

        return Response.noContent().build();
    }

    @GET
    @Path("{evento}/inscricoes/minhas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaMinhasInscricoes(
            @PathParam("evento") Long evento, 
            @QueryParam("pagina") @DefaultValue("1") Integer pagina, 
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaMinhas(evento, new FiltroMinhasInscricoesDTO(pagina, total))).build();
    }
    
    @GET
    @Path("{evento}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("evento") final Long evento){
        return Response.status(Response.Status.OK).entity(appService.buscaEvento(evento)).build();
    }
    
    @DELETE
    @Path("{evento}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("evento") final Long evento){
        appService.removeEvento(evento);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Evento evento){
        for (CampoEvento campo : evento.getCampos()) {
            campo.setEvento(evento);
        }
        return Response.status(Response.Status.OK).entity(appService.cadastra(evento)).build();
    }
    
    @POST
    @Path("{evento}/inscricao")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response realizaInscricao(@PathParam("evento") Long evento, @Valid List<InscricaoEvento> inscricoes){
        Evento entidade = appService.buscaEvento(evento);
        List<InscricaoEvento> merged = new ArrayList<InscricaoEvento>();
        for (InscricaoEvento inscricao : inscricoes){
            if (StringUtil.isEmpty(inscricao.getNomeInscrito()) ||
                    StringUtil.isEmpty(inscricao.getEmailInscrito()) ||
                    StringUtil.isEmpty(inscricao.getTelefoneInscrito())) {
                throw new ServiceException("mensagens.MSG-002");
            }

            InscricaoEvento insc = new InscricaoEvento(entidade);
            MergeUtil.merge(inscricao, View.Cadastro.class).into(insc);
            merged.add(insc);

            if (insc.getValores() != null) {
                for (ValorInscricaoEvento valor : insc.getValores()) {
                    valor.setInscricao(insc);
                }
            }
        }
        return Response.status(Response.Status.OK).entity(appService.realizaInscricao(merged)).build();
    }
    
    @PUT
    @Path("{evento}/confirmar/{inscricao}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmaInscricao(@PathParam("evento") Long evento, @PathParam("inscricao") Long inscricao){
        appService.confirmaInscricao(evento, inscricao);
        return Response.status(Response.Status.OK).build();
    }
    
    @DELETE
    @Path("{evento}/cancelar/{inscricao}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancelaInscricao(@PathParam("evento") Long evento, @PathParam("inscricao") Long inscricao){
        appService.cancelaInscricao(evento, inscricao);
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Evento evento){
        Evento entidade = appService.buscaEvento(evento.getId());
        MergeUtil.merge(evento, View.Edicao.class).into(entidade);
        for (CampoEvento campo : entidade.getCampos()) {
            campo.setEvento(entidade);
        }
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
