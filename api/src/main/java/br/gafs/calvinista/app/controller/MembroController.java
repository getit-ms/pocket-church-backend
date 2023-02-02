/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.ArquivoUtil;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroMembroDTO;
import br.gafs.calvinista.entity.Acesso;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Perfil;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("membro")
public class MembroController {
    
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
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total,
            @QueryParam("pendentes") @DefaultValue("false") final Boolean pendentes){
        return Response.status(Response.Status.OK).entity(appService.busca(new FiltroMembroDTO(nome, email, filtro, pagina, total, perfis, pendentes))).build();
    }
    
    @GET
    @Path("{membro}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("membro") final Long membro){
        return Response.status(Response.Status.OK).entity(appService.buscaMembro(membro)).build();
    }
    
    @DELETE
    @Path("{membro}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("membro") final Long membro){
        appService.removeMembro(membro);
        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("{membro}/acesso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAcessoAdmin(@PathParam("membro") final Long membro){
        return Response.status(Response.Status.OK).entity(appService.buscaAcessoAdmin(membro)).build();
    }

    @GET
    @Path("aniversariantes")
    @JsonView(Membro.Aniversariante.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAniversariantes(){
        return Response.status(Response.Status.OK).entity(appService.buscaProximosAniversariantes()).build();
    }

    @PUT
    @Path("{membro}/redefine-senha")
    @Produces(MediaType.APPLICATION_JSON)
    public Response redefineSenha(@PathParam("membro") final Long membro){
        appService.redefinirSenha(membro);
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("{membro}/cadastro/contato")
    @Produces(MediaType.APPLICATION_JSON)
    public Response aprovaCadastroContato(@PathParam("membro") final Long membro){
        return Response.ok(appService.aprovaCadastroContato(membro)).build();
    }

    @PUT
    @Path("{membro}/cadastro")
    @Produces(MediaType.APPLICATION_JSON)
    public Response aprovaCadastroMembro(@PathParam("membro") final Long membro){
        return Response.ok(appService.aprovaCadastroMembro(membro)).build();
    }

    @DELETE
    @Path("{membro}/cadastro")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejeitaCadastroMembro(@PathParam("membro") final Long membro){
        appService.rejeitaCadastro(membro);
        return Response.ok().build();
    }

    @PUT
    @Path("{membro}/acesso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response darAcessoAdmin(@PathParam("membro") final Long membro, Acesso acesso){
        List<Perfil> perfis = new ArrayList<Perfil>();
        for (Perfil perfil : acesso.getPerfis()){
            perfis.add(appService.buscaPerfil(perfil.getId()));
        }
        
        List<Ministerio> ministerios = new ArrayList<Ministerio>();
        for (Ministerio ministerio : acesso.getMinisterios()){
            ministerios.add(appService.buscaMinisterio(ministerio.getId()));
        }
        
        
        return Response.status(Response.Status.OK).entity(appService.darAcessoAdmin(membro, perfis, ministerios)).build();
    }

    @GET
    @Path("exportar.xls")
    @Produces({"application/xls", MediaType.APPLICATION_JSON})
    public Response exportaInscricoes() throws IOException, InterruptedException {
        File file = relatorioService.exportaContatos();

        response.addHeader("Content-Type", "application/xls");
        response.addHeader("Content-Length", "" + file.length());
        response.addHeader("Content-Disposition",
                "attachment; filename=\""+ file.getName() + "\"");
        ArquivoUtil.transfer(new FileInputStream(file), response.getOutputStream());

        return Response.noContent().build();
    }

    @DELETE
    @Path("{membro}/acesso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retirarAcessoAdmin(@PathParam("membro") final Long membro){
        appService.retiraAcessoAdmin(membro);
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Path("{membro}/membro")
    @Produces(MediaType.APPLICATION_JSON)
    public Response darAcessoMembro(@PathParam("membro") final Long membro){
        return Response.status(Response.Status.OK).entity(appService.darAcessoMembro(membro)).build();
    }
    
    @DELETE
    @Path("{membro}/membro")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retirarAcessoMembro(@PathParam("membro") final Long membro){
        return Response.status(Response.Status.OK).entity(appService.retiraAcessoMembro(membro)).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(final Membro membro){
        return Response.status(Response.Status.OK).entity(appService.cadastra(membro)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON) 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Membro membro){
        Membro entidade = appService.buscaMembro(membro.getId());
        MergeUtil.merge(membro, View.Edicao.class).into(entidade);
        MergeUtil.merge(membro.getEndereco(), View.Edicao.class).into(entidade.getEndereco());
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
