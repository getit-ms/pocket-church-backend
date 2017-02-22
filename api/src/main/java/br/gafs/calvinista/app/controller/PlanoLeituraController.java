/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.MyJacksonJsonProvider;
import br.gafs.calvinista.app.util.MergeUtil;
import br.gafs.calvinista.dto.FiltroPlanoLeituraBiblicaDTO;
import br.gafs.calvinista.entity.DiaLeituraBiblica;
import br.gafs.calvinista.entity.PlanoLeituraBiblica;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;
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
@Path("planoLeitura")
public class PlanoLeituraController {
    
    @EJB
    private AppService appService;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaTodas(
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataTermino") String dataTermino,
            @QueryParam("descricao") String descricao,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(
                appService.buscaTodos(new FiltroPlanoLeituraBiblicaDTO(
                        StringUtil.isEmpty(dataInicio) ? null : 
                                DateUtil.parseData(dataInicio, MyJacksonJsonProvider.DATE_FORMAT),
                        StringUtil.isEmpty(dataTermino) ? null : 
                                DateUtil.parseData(dataTermino, MyJacksonJsonProvider.DATE_FORMAT),
                        descricao, pagina, total))).build();
    }
    
    @PUT
    @Path("leitura/{plano}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response seleciona(@PathParam("plano") Long plano){
        return Response.status(Response.Status.OK).entity(appService.selecionaPlano(plano)).build();
    }
    
    @GET
    @Path("leitura/plano")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPlanoLeituraSelecionado(){
        return Response.status(Response.Status.OK).entity(appService.buscaPlanoSelecionado()).build();
    }
    
    @GET
    @Path("leitura")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaLeituraSelecionada(
            @QueryParam("ultimaAlteracao") String ultimaAlteracao,
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total){
        return Response.status(Response.Status.OK).entity(appService.buscaPlanoSelecionado(
                StringUtil.isEmpty(ultimaAlteracao) ? null :
                        DateUtil.parseData(ultimaAlteracao, MyJacksonJsonProvider.DATE_FORMAT),
                pagina, total)).build();
    }
    
    @DELETE
    @Path("leitura")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desseleciona(){
        appService.desselecionaPlano();
        return Response.status(Response.Status.OK).build();
    }
    
    @PUT
    @Path("leitura/dia/{dia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response leitura(@PathParam("dia") Long dia){
        return Response.status(Response.Status.OK).entity(appService.marcaLeitura(dia)).build();
    }
    
    @DELETE
    @Path("leitura/dia/{dia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desleitura(@PathParam("dia") Long dia){
        return Response.status(Response.Status.OK).entity(appService.desmarcaLeitura(dia)).build();
    }
    
    @GET
    @Path("{plano}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("plano") Long plano){
        return Response.status(Response.Status.OK).entity(appService.buscaPlanoLeitura(plano)).build();
    }
    
    @DELETE
    @Path("{plano}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("plano") Long plano){
        appService.removePlanoLeitura(plano);
        return Response.status(Response.Status.OK).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(PlanoLeituraBiblica plano){
        PlanoLeituraBiblica nova = MergeUtil.merge(plano, View.Edicao.class).into(new PlanoLeituraBiblica());
        
        List<DiaLeituraBiblica> dias = new ArrayList<DiaLeituraBiblica>();
        for (DiaLeituraBiblica dia : nova.getDias()){
            dias.add(MergeUtil.merge(dia, View.Edicao.class).into(new DiaLeituraBiblica()));
        }
        nova.setDias(dias);
        
        return Response.status(Response.Status.OK).entity(appService.cadastra(nova)).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualiza(PlanoLeituraBiblica plano){
        PlanoLeituraBiblica entidade = appService.buscaPlanoLeitura(plano.getId());
        MergeUtil.merge(plano, View.Edicao.class).into(entidade);
        return Response.status(Response.Status.OK).entity(appService.atualiza(entidade)).build();
    }
    
}
