/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.validation;

import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.exception.ValidationException;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;

import javax.inject.Named;

/**
 * @author Gabriel
 */
@Named
@ValidadorNegocial(Evento.class)
public class EventoValidation implements ValidadorServico<Evento> {

    @Override
    public void valida(Evento entidade) throws ServiceException, ServiceExceptionList {
        ValidationException validation = ValidationException.build();

        if (entidade.getDataInicioInscricao() != null && entidade.getDataTerminoInscricao() != null &&
                entidade.getDataInicioInscricao().after(entidade.getDataTerminoInscricao())) {
            validation.
                    add("dataInicioInscricao", "mensagens.MSG-026").
                    add("dataTerminoInscricao", "mensagens.MSG-026");
        }

        if (entidade.getDataHoraInicio() != null && entidade.getDataHoraTermino() != null &&
                entidade.getDataHoraInicio().after(entidade.getDataHoraTermino())) {
            validation.
                    add("dataHoraInicio", "mensagens.MSG-026").
                    add("dataHoraTermino", "mensagens.MSG-026");
        }

        if (entidade.getDataHoraTermino() != null && entidade.getDataTerminoInscricao() != null &&
                entidade.getDataHoraTermino().before(entidade.getDataTerminoInscricao())) {
            validation.
                    add("dataTerminoInscricao", "mensagens.MSG-027");
        }

        validation.validate();
    }


}
