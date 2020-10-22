/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.util.ArquivoUtil;
import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.FiltroColaboradorDTO;
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
@Path("colaborador")
public class ColaboradorController {
    
    @EJB
    private AppService appService;

    @EJB
    private RelatorioService relatorioService;

    @Context
    private HttpServletResponse response;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("nome") @DefaultValue("") final String nome, 
            @QueryParam("email") @DefaultValue("") final String email,
            @QueryParam("filtro") @DefaultValue("") final String filtro,
            @QueryParam("perfil") List<Long> perfis,
            @QueryParam("acessoRecente") Boolean acessoRecente,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.busca(
                new FiltroColaboradorDTO(nome, email, filtro, acessoRecente != null && acessoRecente, pagina, total, perfis))).build();
    }

    @GET
    @Path("lotacao")
    @JsonView(View.Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaLotacoes(){
        return Response.status(Response.Status.OK).entity(appService.buscaLotacoesColaborador()).build();
    }

    @GET
    @Path("{colaborador}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("colaborador") final Long colaborador){
        return Response.status(Response.Status.OK).entity(appService.buscaColaborador(colaborador)).build();
    }
    
    @DELETE
    @Path("{colaborador}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("colaborador") final Long colaborador){
        appService.removeColaborador(colaborador);
        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("{colaborador}/acesso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAcessoAdmin(@PathParam("colaborador") final Long colaborador){
        return Response.status(Response.Status.OK).entity(appService.buscaAcessoAdmin(colaborador)).build();
    }

    @GET
    @Path("aniversariantes")
    @JsonView(Colaborador.Aniversariante.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAniversariantes(){
        return Response.status(Response.Status.OK)
                .entity(appService.buscaProximosAniversariantes()).build();
    }

    @GET
    @Path("aniversariantes/hoje")
    @JsonView(Colaborador.Aniversariante.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAniversariantesHoje(){
        return Response.status(Response.Status.OK)
                .entity(appService.buscaAniversariantesHoje()).build();
    }

    @PUT
    @Path("{colaborador}/redefine-senha")
    @Produces(MediaType.APPLICATION_JSON)
    public Response redefineSenha(@PathParam("colaborador") final Long colaborador){
        appService.redefinirSenha(colaborador);
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("{colaborador}/acesso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response darAcessoAdmin(@PathParam("colaborador") final Long colaborador, Acesso acesso){
        List<Perfil> perfis = new ArrayList<Perfil>();
        for (Perfil perfil : acesso.getPerfis()){
            perfis.add(appService.buscaPerfil(perfil.getId()));
        }

        return Response.status(Response.Status.OK).entity(appService.darAcessoAdmin(colaborador, perfis)).build();
    }

    @GET
    @Path("exportar.xls")
    @Produces({"application/xls", MediaType.APPLICATION_JSON})
    public Response exportaInscricoes(@QueryParam("acessoRecente") Boolean acessoRecente) throws IOException, InterruptedException {
        File file = relatorioService.exportaContatos(acessoRecente != null && acessoRecente);

        response.addHeader("Content-Type", "application/xls");
        response.addHeader("Content-Length", "" + file.length());
        response.addHeader("Content-Disposition",
                "attachment; filename=\""+ file.getName() + "\"");
        ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

        return Response.noContent().build();
    }

    @DELETE
    @Path("{colaborador}/acesso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retirarAcessoAdmin(@PathParam("colaborador") final Long colaborador){
        appService.retiraAcessoAdmin(colaborador);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("lotacao")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final LotacaoColaborador lotacao){
        return Response.status(Response.Status.OK).entity(appService.cadastra(lotacao)).build();
    }
    
    @PUT
    @Path("{colaborador}/colaborador")
    @Produces(MediaType.APPLICATION_JSON)
    public Response darAcessoColaborador(@PathParam("colaborador") final Long colaborador){
        return Response.status(Response.Status.OK).entity(appService.darAcessoColaborador(colaborador)).build();
    }
    
    @DELETE
    @Path("{colaborador}/colaborador")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retirarAcessoColaborador(@PathParam("colaborador") final Long colaborador){
        return Response.status(Response.Status.OK).entity(appService.retiraAcessoColaborador(colaborador)).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response remove(final Colaborador colaborador){
        return Response.status(Response.Status.OK).entity(appService.cadastra(colaborador)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON) 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Colaborador colaborador){
        Colaborador entidade = appService.buscaColaborador(colaborador.getId());
        MergeUtil.merge(colaborador, View.Edicao.class).into(entidade);
        MergeUtil.merge(colaborador.getEndereco(), View.Edicao.class).into(entidade.getEndereco());
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
