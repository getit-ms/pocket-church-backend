/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.validation;

import br.gafs.pocket.corporate.entity.Evento;
import br.gafs.pocket.corporate.entity.InscricaoEvento;
import br.gafs.pocket.corporate.entity.RegistroEmpresaId;
import br.gafs.pocket.corporate.exception.ValidationException;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;
import javax.ejb.EJB;
import javax.inject.Named;

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
        
        Evento evento = daoService.find(Evento.class, new RegistroEmpresaId(entidade.getEvento().getEmpresa().getId(), entidade.getEvento().getId()));
        
        if (entidade.getId() == null){
            if (!evento.isInscricoesAbertas()){
                throw new ServiceException("mensagens.MSG-028");
            }
        }

        validation.validate();
    }

    
}
