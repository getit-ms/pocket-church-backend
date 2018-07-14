/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.validation;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.CalendarioAtendimento;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Perfil;
import br.gafs.calvinista.exception.ValidationException;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.util.string.StringUtil;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;
import javax.ejb.EJB;
import javax.inject.Named;

/**
 *
 * @author Gabriel
 */
@Named
@ValidadorNegocial(Membro.class)
public class MembroValidation implements ValidadorServico<Membro> {
    
    @EJB
    private DAOService daoService;

    @Override
    public void valida(Membro entidade) throws ServiceException, ServiceExceptionList {
        if (!StringUtil.isEmpty(entidade.getEmail())) {
            Membro outro = daoService.findWith(QueryAdmin.MEMBRO_POR_EMAIL_IGREJA.
                    createSingle(entidade.getEmail().toLowerCase(), entidade.getIgreja().getChave()));
            if (outro != null && !outro.getId().equals(entidade.getId())){
                throw new ServiceException("mensagens.MSG-025");
            }
        }

        if (entidade.getId() != null && !entidade.isPastor()){
            CalendarioAtendimento calendario = daoService.findWith(QueryAdmin.
                    CALENDARIO_ATIVO_POR_PASTOR.createSingle(entidade.getIgreja().getChave(), entidade.getId()));
            if (calendario != null){
                throw new ServiceException("mensagens.MSG-039");
            }
        }
    }

    
}
