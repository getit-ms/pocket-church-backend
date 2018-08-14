/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.dto.DTO;
import br.gafs.query.AbstractQuery;
import br.gafs.query.Queries;

/**
 *
 * @author Gabriel
 */
public abstract class AbstractPaginatedFiltro<T extends DTO> extends AbstractQuery implements Queries.PaginatedCustomQuery  {

    public AbstractPaginatedFiltro(T filtro) {
        
    }
    
}
