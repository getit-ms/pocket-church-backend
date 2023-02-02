/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.controller;

import br.gafs.calvinista.app.dto.DateParam;
import br.gafs.calvinista.app.dto.agendamento.RequisicaoAgendamentoDTO;
import br.gafs.calvinista.dto.FiltroMeusAgendamentoDTO;
import br.gafs.calvinista.entity.CalendarioAtendimento;
import br.gafs.calvinista.entity.HorarioAtendimento;
import br.gafs.calvinista.entity.domain.DiaSemana;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;

/**
 * @author Gabriel
 */
@RequestScoped
@Path("agenda")
public class AgendaAtendimentoController {

    @EJB
    private AppService appService;

    @GET
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca() {
        return Response.status(Status.OK).entity(appService.buscaCalendarios()).build();
    }

    @GET
    @Path("pastores")
    @JsonView(Resumido.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaPastores() {
        return Response.status(Status.OK).entity(appService.buscaPastores()).build();
    }

    @GET
    @Path("diasSemana")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaDiasSemana() {
        return Response.status(Status.OK).entity(Arrays.asList(DiaSemana.values())).build();
    }

    @DELETE
    @Path("{agenda}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("agenda") Long agenda) {
        appService.removeCalendario(agenda);
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("{agenda}")
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response busca(@PathParam("agenda") Long agenda) {
        return Response.status(Status.OK).entity(appService.buscaCalendario(agenda)).build();
    }

    @POST
    @JsonView(Detalhado.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastra(CalendarioAtendimento agenda) {
        return Response.status(Status.OK).entity(appService.cadastra(agenda)).build();
    }

    @POST
    @Path("{agenda}/horario")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cadastraHorario(@PathParam("agenda") Long agenda, HorarioAtendimento horario) {
        appService.cadastra(agenda, horario);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("{agenda}/horario/{horario}/dia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("agenda") Long agenda,
                           @PathParam("horario") Long horario,
                           @QueryParam("data") DateParam data) {
        appService.removeDia(agenda, horario, data != null ? data.getData() : null);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{agenda}/horario/{horario}/periodo")
    public Response remove(@PathParam("agenda") Long agenda,
                           @PathParam("horario") Long horario,
                           @QueryParam("inicio") DateParam inicio,
                           @QueryParam("fim") DateParam fim) {
        appService.removePeriodo(agenda, horario,
                inicio != null ? inicio.getData() : null,
                fim != null ? fim.getData() : null);
        return Response.status(Status.OK).build();
    }

    @POST
    @Path("{agenda}/agendar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response agendar(RequisicaoAgendamentoDTO requisicao) {
        return Response.status(Status.OK).entity(appService.agenda(
                requisicao.getMembro(), requisicao.getHorario(), requisicao.getData())).build();
    }

    @POST
    @Path("{agenda}/confirmar/{agendamento}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmar(@PathParam("agenda") Long agenda, @PathParam("agendamento") Long agendamento) {
        return Response.status(Status.OK).entity(appService.confirma(agendamento)).build();
    }

    @POST
    @Path("{agenda}/cancelar/{agendamento}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelar(@PathParam("agenda") Long agenda, @PathParam("agendamento") Long agendamento) {
        return Response.status(Status.OK).entity(appService.cancela(agendamento)).build();
    }

    @GET
    @Path("{agenda}/agendamentos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaAgendamentos(@PathParam("agenda") Long agenda,
                                      @QueryParam("di") DateParam dataInicio, @QueryParam("df") DateParam dataFim) {
        CalendarioAtendimento calendario = appService.buscaCalendario(agenda);
        return Response.status(Status.OK).entity(appService.buscaAgendamentos(calendario,
                dataInicio != null ? dataInicio.getData() : null,
                dataFim != null ? dataFim.getData() : null)).build();
    }

    @GET
    @Path("{agenda}/agenda")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaCalendario(@PathParam("agenda") Long agenda,
                                    @QueryParam("di") DateParam dataInicio, @QueryParam("df") DateParam dataFim) {
        return Response.status(Status.OK).entity(appService.buscaAgenda(agenda,
                dataInicio != null ? dataInicio.getData() : null, dataFim != null ? dataFim.getData() : null)).build();
    }

    @GET
    @Path("agendamentos/meus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscaMeusAgendamentos(
            @QueryParam("pagina") @DefaultValue("1") Integer pagina,
            @QueryParam("total") @DefaultValue("10") Integer total) {
        return Response.status(Status.OK).entity(appService.buscaMeusAgendamentos(new FiltroMeusAgendamentoDTO(pagina, total))).build();
    }


}
