/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.validation;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.RespostaQuestao;
import br.gafs.calvinista.entity.RespostaVotacao;
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
@ValidadorNegocial(RespostaVotacao.class)
public class RespostaVotacaoValidation implements ValidadorServico<RespostaVotacao> {

    @EJB
    private CustomDAOService daoService;

    @Override
    public void valida(RespostaVotacao entidade) throws ServiceException, ServiceExceptionList {
        for (RespostaQuestao rq : entidade.getRespostas()) {
            Questao questao = daoService.find(Questao.class, rq.getQuestao().getId());

            if (!questao.getQuantidadeVotos().equals(rq.getQuantidadeVotos())) {
                throw new ServiceException("mensagens.MSG-032");
            }
        }

    }


}
