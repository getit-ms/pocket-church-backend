/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.validation;

import br.gafs.pocket.corporate.entity.Perfil;
import br.gafs.pocket.corporate.exception.ValidationException;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.validation.ValidadorNegocial;
import br.gafs.validation.ValidadorServico;
import javax.inject.Named;

/**
 *
 * @author Gabriel
 */
@Named
@ValidadorNegocial(Perfil.class)
public class PerfilValidation implements ValidadorServico<Perfil> {

    @Override
    public void valida(Perfil entidade) throws ServiceException, ServiceExceptionList {
        if (entidade.getFuncionalidades().isEmpty()){
            ValidationException.build().add("funcionalidades", "mensagens.MSG-006").validate();
        }
    }

    
}
