/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.validation;

import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.CalendarioAtendimento;
import br.gafs.pocket.corporate.entity.Colaborador;
import br.gafs.pocket.corporate.entity.Colaborador;
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
@ValidadorNegocial(Colaborador.class)
public class ColaboradorValidation implements ValidadorServico<Colaborador> {
    
    @EJB
    private DAOService daoService;

    @Override
    public void valida(Colaborador entidade) throws ServiceException, ServiceExceptionList {
        if (!StringUtil.isEmpty(entidade.getEmail())) {
            Colaborador outro = daoService.findWith(QueryAdmin.COLABORADOR_POR_EMAIL_EMPRESA.
                    createSingle(entidade.getEmail().toLowerCase(), entidade.getEmpresa().getChave()));
            if (outro != null && !outro.getId().equals(entidade.getId())){
                throw new ServiceException("mensagens.MSG-025");
            }
        }

        if (entidade.getId() != null && !entidade.isGerente()){
            CalendarioAtendimento calendario = daoService.findWith(QueryAdmin.
                    CALENDARIO_ATIVO_POR_GERENTE.createSingle(entidade.getEmpresa().getChave(), entidade.getId()));
            if (calendario != null){
                throw new ServiceException("mensagens.MSG-039");
            }
        }
    }

    
}
