package br.gafs.pocket.corporate.app.controller.infra;

import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.service.ParametroService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

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
        ).build();
    }

    @GET
    @Path("i18n/empresa/{empresa}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBundleEmpresa(@PathParam("empresa") String empresa) throws IOException {
        return Response.status(Response.Status.OK).entity(
                parametroService.get(empresa, TipoParametro.BUNDLE_WEB)
        ).build();
    }

    @GET
    @Path("i18n/locale/app/{locale}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBundleLocaleApp(@PathParam("locale") String locale) throws IOException {
        return Response.status(Response.Status.OK).entity(
                parametroService.get(locale, TipoParametro.BUNDLE_APP)
        ).build();
    }

    @GET
    @Path("i18n/empresa/app/{empresa}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBundleEmpresaApp(@PathParam("empresa") String empresa) throws IOException {
        return Response.status(Response.Status.OK).entity(
                parametroService.get(empresa, TipoParametro.BUNDLE_APP)
        ).build();
    }

}

