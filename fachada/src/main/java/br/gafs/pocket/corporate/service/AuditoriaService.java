/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.service;

import br.gafs.pocket.corporate.entity.RegistroAuditoria;
import br.gafs.pocket.corporate.entity.domain.StatusRegistroAuditoria;

import java.io.Serializable;

/**
 *
 * @author Gabriel
 */
public interface AuditoriaService extends Serializable {
    void registra(RegistroAuditoria auditoria, Object request, Object response, StatusRegistroAuditoria status);
}
