/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.ArquivoUtil;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroVotacaoAtivaDTO;
import br.gafs.calvinista.dto.FiltroVotacaoDTO;
import br.gafs.calvinista.entity.*;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.RelatorioService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("votacao")
public class VotacaoController {
    
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
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaTodas(new FiltroVotacaoDTO(null, pagina, total))).build();
    }

    @GET
    @Path("ativas")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaAtivas(
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaAtivas(new FiltroVotacaoAtivaDTO(pagina, total))).build();
    }
    
    @GET
    @Path("{votacao}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("votacao") Long votacao){
        return Response.status(Response.Status.OK).entity(appService.buscaVotacao(votacao)).build();
    }

    @GET
    @Path("{votacao}/resultado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResultado(@PathParam("votacao") Long votacao){
        return Response.status(Response.Status.OK).entity(appService.buscaResultado(votacao)).build();
    }

    @GET
    @Path("{votacao}/resultado/{filename}.{tipo}")
    @Produces({"application/pdf", "application/docx", "application/xls", MediaType.APPLICATION_JSON})
    public Response getResultado(@PathParam("votacao") Long votacao,
                                 @PathParam("tipo") String tipo,
                                 @PathParam("filename") String filename) throws IOException, InterruptedException {
        File file = relatorioService.exportaResultadosVotacao(votacao, tipo);

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
    @Path("{votacao}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("votacao") Long votacao){
        appService.removeVotacao(votacao);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(Votacao votacao){
        Votacao nova = MergeUtil.merge(votacao, View.Edicao.class).into(new Votacao());
        
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
    public Response atualiza(Votacao votacao){
        Votacao entidade = appService.buscaVotacao(votacao.getId());
        MergeUtil.merge(votacao, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
    @POST
    @Path("voto")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response realizaVotacao(RespostaVotacao resposta){
        resposta.setVotacao(appService.buscaVotacao(resposta.getVotacao().getId()));
        for (RespostaQuestao questao : resposta.getRespostas()){
            questao.setQuestao(appService.buscaQuestao(questao.getQuestao().getId()));
            questao.setVotacao(resposta);
            
            for (RespostaOpcao opcao : questao.getOpcoes()){
                opcao.setOpcao(appService.buscaOpcao(opcao.getOpcao().getId()));
                opcao.setQuestao(questao);
            }
        }
        
        appService.realizarVotacao(resposta);
        return Response.status(Response.Status.OK).build();
    }
    
    
    
}
