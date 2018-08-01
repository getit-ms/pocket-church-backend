/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.validation;

import br.gafs.pocket.corporate.entity.Questao;
import br.gafs.pocket.corporate.entity.RespostaEnquete;
import br.gafs.pocket.corporate.entity.RespostaEnqueteColaborador;
import br.gafs.pocket.corporate.entity.RespostaQuestao;
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
@ValidadorNegocial(RespostaEnquete.class)
public class RespostaEnqueteValidation implements ValidadorServico<RespostaEnquete> {
    
    @EJB
    private DAOService daoService;

    @Override
    public void valida(RespostaEnquete entidade) throws ServiceException, ServiceExceptionList {
        for (RespostaQuestao rq : entidade.getRespostas()){
            Questao questao = daoService.find(Questao.class, rq.getQuestao().getId());
            
            if (!questao.getQuantidadeRespostasEnqueteColaborador().equals(rq.getQuantidadeRespostaEnqueteColaboradors())){
                throw new ServiceException("mensagens.MSG-032");
            }
        }
        
    }

    
}
