package br.gafs.calvinista.validation;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.CategoriaEstudo;
import br.gafs.calvinista.exception.ValidationException;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;

import javax.ejb.EJB;
import javax.inject.Named;

/**
 * Created by Gabriel on 15/02/2018.
 */
@Named
@ValidadorNegocial(CategoriaEstudo.class)
public class CategoriaEstudoValidation implements ValidadorServico<CategoriaEstudo> {

    @EJB
    private DAOService daoService;

    @Override
    public void valida(CategoriaEstudo entidade) throws ServiceException, ServiceExceptionList {
        ValidationException validation = ValidationException.build();

        CategoriaEstudo outra = daoService.findWith(QueryAdmin.CATEGORIA_ESTUDO_POR_IGREJA_NOME
                .createSingle(entidade.getChaveIgreja(), entidade.getNome().toLowerCase()));

        if (outra != null && !outra.getId().equals(entidade.getId())) {
            validation.add("nome", "mensagens.MSG-048");
        }

        validation.validate();
    }

}
