/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller.infra;

import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
@RequestScoped
@Path("assets")
public class AssetsController {

    @EJB
    private ParametroService parametroService;

    @GET
    @Path("i18n/locale/{locale}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBundleLocale(@PathParam("locale") String locale) throws IOException {
        return Response.status(Response.Status.OK).entity(
                parametroService.get(locale, TipoParametro.BUNDLE_WEB)
        ).encoding("ISO-8859-1").build();
    }

    @GET
    @Path("i18n/igreja/{igreja}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBundleIgreja(@PathParam("igreja") String igreja) throws IOException {
        return Response.status(Response.Status.OK).entity(
                parametroService.get(igreja, TipoParametro.BUNDLE_WEB)
        ).encoding("ISO-8859-1").build();
    }

}
