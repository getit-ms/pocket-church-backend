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
import br.gafs.calvinista.entity.Usuario;
import br.gafs.calvinista.entity.domain.HorasEnvioVersiculo;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.util.senha.SenhaUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
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
@Path("acesso")
@RequestScoped
public class AcessoController {
    
    @EJB
    private AcessoService acessoService;
    
    @EJB
    private AppService appService;
    
    @PUT
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response realizaLogin(final RequisicaoLoginDTO req){
        String auth = acessoService.login(req.getUsername(), SenhaUtil.encryptSHA256(req.getPassword()));
        acessoService.registerPush(req.getTipoDispositivo(), null, req.getVersion());
        return Response.status(Response.Status.OK).entity(acesso(auth)).build();
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
        Membro entidade = acessoService.getMembro();
        MergeUtil.merge(membro, View.AlterarSenha.class).into(entidade);
        acessoService.alteraSenha(entidade);
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
        return Response.status(Response.Status.OK).entity(Arrays.asList(HorasEnvioVersiculo.values())).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaAcesso(){
        return Response.status(Response.Status.OK).entity(acesso(null)).build();
    }
    
    private AcessoDTO acesso(String auth){
        return new AcessoDTO(
                acessoService.getMembro(),
                acessoService.getFuncionalidades(),
                auth);
    }
}
