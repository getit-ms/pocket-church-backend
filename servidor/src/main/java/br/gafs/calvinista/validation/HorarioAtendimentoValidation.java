/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.validation;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.entity.HorarioAtendimento;
import br.gafs.calvinista.exception.ValidationException;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;

import javax.ejb.EJB;
import javax.inject.Named;

/**
 * @author Gabriel
 */
@Named
@ValidadorNegocial(HorarioAtendimento.class)
public class HorarioAtendimentoValidation implements ValidadorServico<HorarioAtendimento> {

    @EJB
    private CustomDAOService daoService;

    @Override
    public void valida(HorarioAtendimento entidade) throws ServiceException, ServiceExceptionList {
        ValidationException validation = ValidationException.build();

//        AgendamentoAtendimento agendamento = daoService.findWith(QueryAdmin.HORARIOS_POR_PERIODO.
//                createSingle(entidade.getCalendario().getId(), 
//                        entidade.getDataHoraInicio(), 
//                        entidade.getDataHoraFim()));
//        
//        if (agendamento != null && !agendamento.getId().equals(entidade.getId())){
//            validation.add("horario", "mensagens.MSG-003");
//        }

        validation.validate();
    }


}
