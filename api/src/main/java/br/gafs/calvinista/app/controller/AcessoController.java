/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.dto.AcessoDTO;
import br.gafs.calvinista.app.dto.acesso.RequisicaoLoginDTO;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.MenuDTO;
import br.gafs.calvinista.entity.Arquivo;
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
import br.gafs.util.string.StringUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
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
    public Response iniciaLogin(final @PathParam("email") String email) {
        return Response.status(Response.Status.OK).entity(acessoService.inciaLogin(email.trim())).build();
    }

    @PUT
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response realizaLogin(final RequisicaoLoginDTO req) {
        Membro membro = acessoService.login(req.getUsername().trim(),
                SenhaUtil.encryptSHA256(req.getPassword()), req.getTipoDispositivo(), req.getVersion());
        return Response.status(Response.Status.OK).entity(acesso(membro, req.getTipoDispositivo(), req.getVersion())).build();
    }

    @PUT
    @Path("logout")
    public Response realizaLogout() {
        acessoService.logout();
        return Response.ok().build();
    }

    @DELETE
    @Path("conta")
    public Response removeConta() {
        acessoService.removeConta();
        return Response.ok().build();
    }

    @GET
    @Path("funcionalidades/publicas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaFuncionalidadesPublicas() {
        return Response.status(Response.Status.OK).entity(acessoService.buscaFuncionalidadesPublicas()).build();
    }

    @GET
    @Path("preferencias")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPreferencias() {
        return Response.status(Response.Status.OK).entity(acessoService.buscaPreferencis()).build();
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaStatus() {
        return Response.status(Response.Status.OK).entity(appService.buscaStatus()).build();
    }

    @GET
    @Path("releaseNotes/{tipoVersao}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaReleaseNotes(@PathParam("tipoVersao") TipoVersao tipoVersao) {
        return Response.status(Response.Status.OK).entity(appService.buscaReleaseNotes(tipoVersao)).build();
    }

    @PUT
    @Path("preferencias")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response salvaPreferencias(Preferencias preferencias) {
        Preferencias entidade = acessoService.buscaPreferencis();
        MergeUtil.merge(preferencias, View.Edicao.class).into(entidade);

        entidade.getMinisteriosInteresse().clear();
        for (Ministerio ministerio : preferencias.getMinisteriosInteresse()) {
            entidade.getMinisteriosInteresse().add(appService.buscaMinisterio(ministerio.getId()));
        }

        return Response.status(Response.Status.OK).entity(acessoService.salva(entidade)).build();
    }

    @PUT
    @Path("foto")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response trocaFoto(Arquivo arquivo) {
        acessoService.trocaFoto(arquivo);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("registerPush")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPushToken(AcessoDTO dto) {
        if (dto != null && dto.getTipoDispositivo() != null && !StringUtil.isEmpty(dto.getToken())) {
            acessoService.registerPush(TipoDispositivo.values()[dto.getTipoDispositivo()], dto.getToken(), dto.getVersion());
        }

        return Response.ok().build();
    }

    @PUT
    @Path("senha/altera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizaSenha(Membro membro) {
        acessoService.alteraSenha(membro);
        return Response.ok().build();
    }

    @PUT
    @Path("senha/redefinir/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response solicitarRedefinirSenha(@PathParam("email") @DefaultValue("") String email) throws UnsupportedEncodingException {
        acessoService.solicitaRedefinicaoSenha(email.trim());
        return Response.ok().build();
    }

    @GET
    @Path("senha/redefinir")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response redefinirSenha(@QueryParam("chave") String jwt) {
        return Response.ok().entity(acessoService.redefineSenha(jwt)).build();
    }

    @GET
    @Path("ministerios")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaMinisterios() {
        return Response.status(Response.Status.OK).entity(acessoService.buscaMinisterios()).build();
    }

    @GET
    @Path("horariosVersiculoDiario")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaHorariosVersiculosDiarios() {
        return Response.status(Response.Status.OK).entity(Arrays.asList(HorasEnvioNotificacao.values())).build();
    }

    @GET
    @Path("horariosLembretesLeitura")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaHorariosLembretesLeitura() {
        return Response.status(Response.Status.OK).entity(Arrays.asList(HorasEnvioNotificacao.values())).build();
    }

    @GET
    @Path("menu")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaMenu(
            @QueryParam("versao") String versao
    ) {
        return Response.status(Response.Status.OK)
                .entity(getMenu(versao)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response renovaAcesso(
            @QueryParam("versao") String versao
    ) {
        return Response.status(Response.Status.OK)
                .entity(acesso(acessoService.refreshLogin(), null, versao)).build();
    }

    private AcessoDTO acesso(Membro membro, TipoDispositivo tipoDispositivo, String versao) {
        return new AcessoDTO(membro, acessoService.getFuncionalidadesMembro(),
                response.getHeader("Set-Authorization"),
                TipoDispositivo.PC.equals(tipoDispositivo) ? null : getMenu(versao),
                acessoService.isExigeAceiteTermo());
    }

    private MenuDTO getMenu(String versao) {
        String vals[] = (StringUtil.isEmpty(versao) || !versao.matches("\\d+\\.\\d+\\.\\d+") ? "0.0.0" : versao).split("\\.");
        return acessoService.buscaMenu(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]));
    }
}
