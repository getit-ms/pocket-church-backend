/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.validation;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.DiaDevocionario;
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
@ValidadorNegocial(DiaDevocionario.class)
public class DiaDevocionarioValidation implements ValidadorServico<DiaDevocionario> {

    @EJB
    private CustomDAOService daoService;

    @Override
    public void valida(DiaDevocionario entidade) throws ServiceException, ServiceExceptionList {
        DiaDevocionario outro = daoService.findWith(QueryAdmin.DEVOCIONARIO_POR_IGREJA_DATA.createSingle(entidade.getIgreja().getChave(), entidade.getData()));

        if (outro != null && !outro.equals(entidade)) {
            throw new ServiceException("mensagens.MSG-061");
        }
    }


}
