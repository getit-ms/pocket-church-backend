/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller.infra;

import br.gafs.calvinista.app.controller.*;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroIgrejaDTO;
import br.gafs.calvinista.dto.ParametrosIgrejaDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Institucional;
import br.gafs.calvinista.entity.domain.TemplateIgreja;
import br.gafs.calvinista.service.AdminService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Arrays;
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
@Path("infra/igreja")
public class IgrejaController {
    
    @EJB
    private AdminService adminService;
    
    @EJB
    private ParametroService paramService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("pagina") @DefaultValue("1") final Integer pagina,
            @QueryParam("total") @DefaultValue("10") final Integer total){
        return Response.status(Response.Status.OK).entity(adminService.busca(new FiltroIgrejaDTO(null, null, pagina, total))).build();
    }
    
    @GET
    @Path("templates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTemplates(){
        return Response.status(Response.Status.OK).entity(Arrays.asList(TemplateIgreja.values())).build();
    }
    
    @GET
    @Path("{igreja}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("igreja") String igreja){
        return Response.status(Response.Status.OK).entity(adminService.buscaIgreja(igreja)).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadatra(final Igreja igreja){
        adminService.cadastra(igreja);
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(final Igreja igreja){
        Igreja entidade = adminService.buscaIgreja(igreja.getChave());
        MergeUtil.merge(igreja, View.Edicao.class).into(entidade);
        adminService.atualiza(entidade);
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Path("{igreja}/parametros")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(@PathParam("igreja") String id, final ParametrosIgrejaDTO parametros){
        paramService.salvaParametros(parametros, id);
        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("{igreja}/parametros")
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualiza(@PathParam("igreja") String id){
        return Response.status(Response.Status.OK).entity(paramService.buscaParametros(id)).build();
    }
    
    @DELETE
    @Path("{igreja}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("igreja") Long igreja){
        adminService.inativa(igreja);
        return Response.status(Response.Status.OK).build();
    }
    
}
