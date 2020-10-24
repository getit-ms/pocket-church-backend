/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.ArquivoUtil;
import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.FiltroEnqueteAtivaDTO;
import br.gafs.pocket.corporate.dto.FiltroEnqueteDTO;
import br.gafs.pocket.corporate.entity.*;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.service.RelatorioService;
import br.gafs.pocket.corporate.view.View;
import br.gafs.pocket.corporate.view.View.Detalhado;
import br.gafs.pocket.corporate.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
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
@Path("enquete")
public class EnqueteController {
    
    @EJB
    private AppService appService;

    @EJB
    private RelatorioService relatorioService;

    @Context
    private HttpServletResponse response;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaTodas(
            @QueryParam("nome") String nome,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodas(new FiltroEnqueteDTO(null, nome, pagina, total))).build();
    }

    @GET
    @Path("ativas")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaAtivas(
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaAtivas(new FiltroEnqueteAtivaDTO(pagina, total))).build();
    }
    
    @GET
    @Path("{enquete}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("enquete") Long enquete){
        return Response.status(Response.Status.OK).entity(appService.buscaEnquete(enquete)).build();
    }

    @GET
    @Path("{enquete}/resultado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResultado(@PathParam("enquete") Long enquete){
        return Response.status(Response.Status.OK).entity(appService.buscaResultado(enquete)).build();
    }

    @GET
    @Path("{enquete}/resultado/{filename}.{tipo}")
    @Produces({"application/pdf", "application/docx", "application/xls", MediaType.APPLICATION_JSON})
    public Response getResultado(@PathParam("enquete") Long enquete,
                                 @PathParam("tipo") String tipo,
                                 @PathParam("filename") String filename) throws IOException, InterruptedException {
        File file = relatorioService.exportaResultadosEnquete(enquete, tipo);

        if (file.getName().startsWith(filename)){
            response.addHeader("Content-Type", "application/" + tipo);
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("Content-Disposition",
                    "attachment; filename=\""+ file.getName() + "\"");
            ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    @DELETE
    @Path("{enquete}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("enquete") Long enquete){
        appService.removeEnquete(enquete);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(Enquete enquete){
        Enquete nova = MergeUtil.merge(enquete, View.Edicao.class).into(new Enquete());
        
        List<Questao> questoes = new ArrayList<Questao>();
        for (Questao questao : nova.getQuestoes()){
            questoes.add(MergeUtil.merge(questao, View.Edicao.class).into(new Questao()));
        }
        nova.setQuestoes(questoes);
        
        return Response.status(Response.Status.OK).entity(appService.cadastra(nova)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(Enquete enquete){
        Enquete entidade = appService.buscaEnquete(enquete.getId());
        MergeUtil.merge(enquete, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
    @POST
    @Path("voto")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response realizaEnquete(RespostaEnquete resposta){
        resposta.setEnquete(appService.buscaEnquete(resposta.getEnquete().getId()));
        for (RespostaQuestao questao : resposta.getRespostas()){
            questao.setQuestao(appService.buscaQuestao(questao.getQuestao().getId()));
            questao.setEnquete(resposta);
            
            for (RespostaOpcao opcao : questao.getOpcoes()){
                opcao.setOpcao(appService.buscaOpcao(opcao.getOpcao().getId()));
                opcao.setQuestao(questao);
            }
        }
        
        appService.realizarEnquete(resposta);
        return Response.status(Response.Status.OK).build();
    }
    
    
    
}
