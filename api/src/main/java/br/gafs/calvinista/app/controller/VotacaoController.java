/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.ReportBuilder;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroVotacaoAtivaDTO;
import br.gafs.calvinista.dto.FiltroVotacaoDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.RespostaOpcao;
import br.gafs.calvinista.entity.RespostaQuestao;
import br.gafs.calvinista.entity.RespostaVotacao;
import br.gafs.calvinista.entity.Votacao;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    private AcessoService acessoService;
    
    @EJB
    private ParametroService paramService;

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
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    public Response getResultado(@PathParam("votacao") Long votacao) throws Exception {
        Votacao entidade = appService.buscaVotacao(votacao);
        return Response.ok(
                ReportBuilder.init(entidade.getIgreja(), paramService.buscaParametros(entidade.getIgreja().getChave())).
                        template("jasper/resultado_votacao.jasper").
                        value(appService.buscaResultado(votacao)).build()
                
        , "application/pdf").build();
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
