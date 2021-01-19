package br.gafs.pocket.corporate.validation;

import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.LotacaoColaborador;
import br.gafs.pocket.corporate.exception.ValidationException;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;

import javax.ejb.EJB;
import javax.inject.Named;

/**
 * Created by Gabriel on 15/02/2018.
 */
@Named
@ValidadorNegocial(LotacaoColaborador.class)
public class LotacaoColaboradorValidation implements ValidadorServico<LotacaoColaborador> {

    @EJB
    private DAOService daoService;

    @Override
    public void valida(LotacaoColaborador entidade) throws ServiceException, ServiceExceptionList {
        ValidationException validation = ValidationException.build();

        LotacaoColaborador outra = daoService.findWith(QueryAdmin.LOTACAO_COLABORADOR_POR_EMPRESA_NOME
                .createSingle(entidade.getChaveEmpresa(), entidade.getNome().toLowerCase()));

        if (outra != null && !outra.getId().equals(entidade.getId())) {
            validation.add("nome", "mensagens.MSG-053");
        }

        validation.validate();
    }

}
