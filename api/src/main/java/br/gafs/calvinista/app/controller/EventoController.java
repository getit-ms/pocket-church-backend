/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.app.util.ReportUtil;
import br.gafs.calvinista.dto.FiltroEventoDTO;
import br.gafs.calvinista.dto.FiltroEventoFuturoDTO;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.dto.FiltroMinhasInscricoesDTO;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.view.relatorio.BuscaPaginadaDataSource;
import net.sf.jasperreports.engine.JRException;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        return Response.status(Response.Status.OK).entity(appService.buscaTodas(evento, new FiltroInscricaoDTO(pagina, total))).build();
    }

    @GET
    @Path("{evento}/inscricoes/{tipo}")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    public Response exportaInscricoes(
            @PathParam("evento") Long id,
            @PathParam("tipo") String tipo) throws JRException, IOException {
        final Evento evento = appService.buscaEvento(id);

        if (evento != null){
            byte[] report = ReportUtil.igreja(
                        "/WEB-INF/report/inscritos_evento.jasper",
                        evento.getNome(),
                        evento.getIgreja(),
                        request.getServletContext())
                    .arg("EVENTO", evento)
                    .dataSource(new BuscaPaginadaDataSource<>(new BuscaPaginadaDataSource.PaginaResolver<InscricaoEvento>() {
                        @Override
                        public BuscaPaginadaDTO<InscricaoEvento> buscaPagina(int pagina) {
                            return appService.buscaTodas(evento.getId(), new FiltroInscricaoDTO(pagina, 30));
                        }
                    })).build().export(tipo);

            response.addHeader("Content-Type", "application/pdf");
            response.addHeader("Content-Length", "" + report.length);
            response.addHeader("Content-Disposition",
                    "attachment; filename=\""+evento.getNome()+"\"." + tipo);
            response.getOutputStream().write(report);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
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
        return Response.status(Response.Status.OK).entity(appService.cadastra(evento)).build();
    }
    
    @POST
    @Path("{evento}/inscricao")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response realizaInscricao(@PathParam("evento") Long evento, List<InscricaoEvento> inscricoes){
        Evento entidade = appService.buscaEvento(evento);
        List<InscricaoEvento> merged = new ArrayList<InscricaoEvento>();
        for (InscricaoEvento inscricao : inscricoes){
            InscricaoEvento insc = new InscricaoEvento(entidade);
            MergeUtil.merge(inscricao, View.Edicao.class).into(insc);
            merged.add(insc);
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
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
