/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.ParametrosIgrejaDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.geracao.Geracao;
import br.gafs.calvinista.geracao.RequisicaoExecucao;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
public interface GeradorService extends Serializable {
    void schedule(int prioridade, RequisicaoExecucao requisicao);
    List<Execucao> getFinished();
    List<Execucao> getPool();
    
    
    @Getter
    @RequiredArgsConstructor
    public class Execucao implements Comparable<Execucao>, Serializable {
        private final int prioridade;
        private final Igreja igreja;
        private final Geracao geracao;
        private File output;

        @Override
        public int compareTo(Execucao o) {
            return prioridade - o.prioridade;
        }

        public void exec(ParametrosIgrejaDTO params) throws Exception {
            output = geracao.execute(igreja, params);
        }
    }
}
