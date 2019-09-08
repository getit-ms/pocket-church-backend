/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller;

import br.gafs.pocket.corporate.app.dto.AcessoDTO;
import br.gafs.pocket.corporate.app.dto.acesso.RequisicaoLoginDTO;
import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.MenuDTO;
import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.pocket.corporate.entity.Colaborador;
import br.gafs.pocket.corporate.entity.Preferencias;
import br.gafs.pocket.corporate.entity.domain.HorasEnvioNotificacao;
import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import br.gafs.pocket.corporate.entity.domain.TipoVersao;
import br.gafs.pocket.corporate.service.AcessoService;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.view.View;
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

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
    
    @GET
    @Path("login/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciaLogin(final @PathParam("email") String email){
        return Response.status(Response.Status.OK).entity(acessoService.inciaLogin(email)).build();
    }

    @PUT
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response realizaLogin(final RequisicaoLoginDTO req){
        Colaborador colaborador = acessoService.login(req.getUsername(),
                SenhaUtil.encryptSHA256(req.getPassword()), req.getTipoDispositivo(), req.getVersion());
        return Response.status(Response.Status.OK).entity(acesso(colaborador, req.getVersion())).build();
    }
    
    @PUT
    @Path("logout")
    public Response realizaLogout(){
        acessoService.logout();
        return Response.ok().build();
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

        return Response.status(Response.Status.OK).entity(acessoService.salva(entidade)).build();
    }

    @PUT
    @Path("foto")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response trocaFoto(Arquivo arquivo){
        acessoService.trocaFoto(arquivo);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("registerPush")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPushToken(AcessoDTO dto){
        if (dto != null && dto.getTipoDispositivo() != null && !StringUtil.isEmpty(dto.getToken())) {
            acessoService.registerPush(TipoDispositivo.values()[dto.getTipoDispositivo()], dto.getToken(), dto.getVersion());
        }

        return Response.ok().build();
    }
    
    @PUT
    @Path("senha/altera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizaSenha(Colaborador colaborador){
        acessoService.alteraSenha(colaborador);
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
    @Path("horariosMensagemDia")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaHorariosMensagensDia(){
        return Response.status(Response.Status.OK).entity(Arrays.asList(HorasEnvioNotificacao.values())).build();
    }
    
    @GET
    @Path("menu")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaMenu(
            @QueryParam("versao") String versao
    ){
        return Response.status(Response.Status.OK)
                .entity(getMenu(versao)).build();
    }
    
    @GET
    @Deprecated
    @Produces(MediaType.APPLICATION_JSON)
    public Response renovaAcesso(
            @QueryParam("versao") String versao
    ){
        return Response.status(Response.Status.OK)
                .entity(acesso(acessoService.refreshLogin(), versao)).build();
    }

    private AcessoDTO acesso(Colaborador colaborador, String versao){
        return new AcessoDTO(colaborador, acessoService.getFuncionalidadesColaborador(),
                response.getHeader("Set-Authorization"), getMenu(versao));
    }

    private MenuDTO getMenu(String versao) {
        String vals[] = (StringUtil.isEmpty(versao) || !versao.matches("\\d+\\.\\d+\\.\\d+") ? "0.0.0" : versao).split("\\.");
        return acessoService.buscaMenu(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]));
    }
}
