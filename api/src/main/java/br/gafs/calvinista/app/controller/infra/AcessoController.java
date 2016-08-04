/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller.infra;

import br.gafs.calvinista.app.dto.AcessoDTO;
import br.gafs.calvinista.app.dto.acesso.RequisicaoLoginDTO;
import br.gafs.calvinista.entity.Usuario;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.util.senha.SenhaUtil;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("infra/acesso")
public class AcessoController {
    
    @EJB
    private AcessoService acessoService;
    
    @PUT
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response realizaLogin(final RequisicaoLoginDTO req){
        String auth = acessoService.admin(req.getUsername(), SenhaUtil.encryptSHA256(req.getPassword()));
        return Response.status(Response.Status.OK).entity(new AcessoDTO(acessoService.getUsuario(), auth)).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaAcesso(){
        return Response.status(Response.Status.OK).entity(new AcessoDTO(acessoService.getUsuario(), null)).build();
    }
}
