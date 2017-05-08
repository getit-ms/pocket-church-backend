/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.dto.AcessoDTO;
import br.gafs.calvinista.app.dto.acesso.RequisicaoLoginDTO;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Preferencias;
import br.gafs.calvinista.entity.domain.HorasEnvioNotificacao;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.entity.domain.TipoVersao;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.util.senha.SenhaUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

/**
 *
 * @author Gabriel
 */
@Path("acesso")
@RequestScoped
public class AcessoController {
    
    @EJB
    private AcessoService acessoService;
    
    @EJB
    private AppService appService;
    
    @Context
    private HttpServletResponse response;
    
    @PUT
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response realizaLogin(final RequisicaoLoginDTO req){
        Membro membro = acessoService.login(req.getUsername(), 
                SenhaUtil.encryptSHA256(req.getPassword()), req.getTipoDispositivo(), req.getVersion());
        return Response.status(Response.Status.OK).entity(acesso(membro)).build();
    }
    
    @PUT
    @Path("logout")
    public Response realizaLogout(){
        acessoService.logout();
        return Response.ok().build();
    }
    
    @GET
    @Path("funcionalidades/publicas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaFuncionalidadesPublicas(){
        return Response.status(Response.Status.OK).entity(acessoService.buscaFuncionalidadesPublicas()).build();
    }
    
    @GET
    @Path("preferencias")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPreferencias(){
        return Response.status(Response.Status.OK).entity(acessoService.buscaPreferencis()).build();
    }
    
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaStatus(){
        return Response.status(Response.Status.OK).entity(appService.buscaStatus()).build();
    }

    @GET
    @Path("releaseNotes/{tipoVersao}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaReleaseNotes(@PathParam("tipoVersao") TipoVersao tipoVersao){
        return Response.status(Response.Status.OK).entity(appService.buscaReleaseNotes(tipoVersao)).build();
    }

    @PUT
    @Path("preferencias")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response salvaPreferencias(Preferencias preferencias){
        Preferencias entidade = acessoService.buscaPreferencis();
        MergeUtil.merge(preferencias, View.Edicao.class).into(entidade);
        
        entidade.getMinisteriosInteresse().clear();
        for (Ministerio ministerio : preferencias.getMinisteriosInteresse()){
            entidade.getMinisteriosInteresse().add(appService.buscaMinisterio(ministerio.getId()));
        }
        
        return Response.status(Response.Status.OK).entity(acessoService.salva(entidade)).build();
    }
    
    @POST
    @Path("registerPush")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPushToken(AcessoDTO dto){
        acessoService.registerPush(TipoDispositivo.values()[dto.getTipoDispositivo()], dto.getToken(), dto.getVersion());
        return Response.ok().build();
    }
    
    @PUT
    @Path("senha/altera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizaSenha(Membro membro){
        acessoService.alteraSenha(membro);
        return Response.ok().build();
    }
    
    @PUT
    @Path("senha/redefinir/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response solicitarRedefinirSenha(@PathParam("email") String email){
        acessoService.solicitaRedefinicaoSenha(email);
        return Response.ok().build();
    }
    
    @GET
    @Path("senha/redefinir")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response redefinirSenha(@QueryParam("chave") String jwt){
        return Response.ok().entity(acessoService.redefineSenha(jwt)).build();
    }
    
    @GET
    @Path("ministerios")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaMinisterios(){
        return Response.status(Response.Status.OK).entity(acessoService.buscaMinisterios()).build();
    }
    
    @GET
    @Path("horariosVersiculoDiario")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaHorariosVersiculosDiarios(){
        return Response.status(Response.Status.OK).entity(Arrays.asList(HorasEnvioNotificacao.values())).build();
    }
    
    @GET
    @Path("horariosLembretesLeitura")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaHorariosLembretesLeitura(){
        return Response.status(Response.Status.OK).entity(Arrays.asList(HorasEnvioNotificacao.values())).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response renovaAcesso(){
        return Response.status(Response.Status.OK).entity(acesso(acessoService.refreshLogin())).build();
    }
    
    private AcessoDTO acesso(Membro membro){
        return new AcessoDTO(membro, acessoService.getFuncionalidadesMembro(), response.getHeader("Set-Authorization"));
    }
}
