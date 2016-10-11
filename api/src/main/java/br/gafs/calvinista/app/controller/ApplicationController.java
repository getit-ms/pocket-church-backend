/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.dto.ContatoDTO;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.MensagemService;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Gabriel
 */
@Path("app")
@RequestScoped
public class ApplicationController {
    
    @Inject
    private HttpServletRequest request;
    
    @EJB
    private MensagemService mensagemService;
    
    @GET
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    public Response version(){
        return Response.status(Response.Status.OK).entity(
                new Versao(request.getServletContext().getInitParameter("projectVersion"))).build();
    }
    
    @POST
    @Path("contato")
    @Produces(MediaType.APPLICATION_JSON)
    public Response contato(ContatoDTO contato){
        mensagemService.enviarMensagem(contato);
        return Response.status(Response.Status.OK).build();
    }
    
    public class Versao {
        private String version;

        public Versao(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}
