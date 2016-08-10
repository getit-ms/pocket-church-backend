/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroMembroDTO;
import br.gafs.calvinista.entity.Acesso;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Perfil;
import br.gafs.calvinista.service.AppService;
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
@Path("membro")
public class MembroController {
    
    @EJB
    private AppService appService;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("nome") @DefaultValue("") final String nome, 
            @QueryParam("email") @DefaultValue("") final String email,
            @QueryParam("filtro") @DefaultValue("") final String filtro,
            @QueryParam("perfil") List<Long> perfis,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(appService.busca(new FiltroMembroDTO(nome, email, filtro, pagina, total, perfis))).build();
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
    public Response remove(final Membro membro){
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
