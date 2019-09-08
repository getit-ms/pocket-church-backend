/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app.controller.infra;

import br.gafs.pocket.corporate.app.util.MergeUtil;
import br.gafs.pocket.corporate.dto.FiltroEmpresaDTO;
import br.gafs.pocket.corporate.dto.ParametrosEmpresaDTO;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.service.AdminService;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.pocket.corporate.view.View;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("infra/empresa")
public class EmpresaController {
    
    @EJB
    private AdminService adminService;
    
    @EJB
    private ParametroService paramService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("chave") String chave,
            @QueryParam("filtro") String filtro,
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(adminService.busca(new FiltroEmpresaDTO(chave, filtro, pagina, total))).build();
    }
    
    @GET
    @Path("{empresa}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("empresa") String empresa){
        return Response.status(Response.Status.OK).entity(adminService.buscaEmpresa(empresa)).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadatra(final Empresa empresa){
        adminService.cadastra(empresa);
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Empresa empresa){
        Empresa entidade = adminService.buscaEmpresa(empresa.getChave());
        MergeUtil.merge(empresa, View.Edicao.class).into(entidade);
        adminService.atualiza(entidade);
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Path("{empresa}/parametros")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(@PathParam("empresa") String id, final ParametrosEmpresaDTO parametros){
        paramService.salvaParametros(parametros, id);
        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("{empresa}/parametros")
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualiza(@PathParam("empresa") String id){
        return Response.status(Response.Status.OK).entity(paramService.buscaParametros(id)).build();
    }
    
    @DELETE
    @Path("{empresa}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("empresa") Long empresa){
        adminService.inativa(empresa);
        return Response.status(Response.Status.OK).build();
    }
    
}
