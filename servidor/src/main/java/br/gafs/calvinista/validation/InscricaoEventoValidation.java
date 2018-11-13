/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.validation;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.exception.ValidationException;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Named
@ValidadorNegocial(InscricaoEvento.class)
public class InscricaoEventoValidation implements ValidadorServico<InscricaoEvento> {
    
    @EJB
    private DAOService daoService;

    @Override
    public void valida(InscricaoEvento entidade) throws ServiceException, ServiceExceptionList {
        ValidationException validation = ValidationException.build();
        
        Evento evento = daoService.find(Evento.class, new RegistroIgrejaId(entidade.getEvento().getIgreja().getId(), entidade.getEvento().getId()));
        
        if (entidade.getId() == null){
            if (!evento.isInscricoesAbertas()){
                throw new ServiceException("mensagens.MSG-028");
            }
        }

        validation.validate();
    }

    
}