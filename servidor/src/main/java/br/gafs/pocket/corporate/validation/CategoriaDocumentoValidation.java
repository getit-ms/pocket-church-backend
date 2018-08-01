package br.gafs.pocket.corporate.validation;

import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.CategoriaDocumento;
import br.gafs.pocket.corporate.exception.ValidationException;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;

import javax.ejb.EJB;
import javax.inject.Named;

/**
 * Created by Gabriel on 15/02/2018.
 */
@Named
@ValidadorNegocial(CategoriaDocumento.class)
public class CategoriaDocumentoValidation implements ValidadorServico<CategoriaDocumento> {

    @EJB
    private DAOService daoService;

    @Override
    public void valida(CategoriaDocumento entidade) throws ServiceException, ServiceExceptionList {
        ValidationException validation = ValidationException.build();

        CategoriaDocumento outra = daoService.findWith(QueryAdmin.CATEGORIA_DOCUMENTO_POR_EMPRESA_NOME
                .createSingle(entidade.getChaveEmpresa(), entidade.getNome().toLowerCase()));

        if (outra != null && !outra.getId().equals(entidade.getId())) {
            validation.add("nome", "mensagens.MSG-048");
        }

        validation.validate();
    }

}
