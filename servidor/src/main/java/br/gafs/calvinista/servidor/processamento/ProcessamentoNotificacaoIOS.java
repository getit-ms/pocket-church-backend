/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor.processamento;

import br.gafs.calvinista.dao.FiltroDispositivoNotificacao;
import br.gafs.calvinista.dao.RegisterSentNotifications;
import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.servidor.MensagemServiceImpl;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.servidor.mensagem.AndroidNotificationService;
import br.gafs.calvinista.servidor.mensagem.IOSNotificationService;
import br.gafs.dao.BuscaPaginadaDTO;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

/**
 *
 * @author Gabriel
 */
@AllArgsConstructor
public class ProcessamentoNotificacaoIOS implements ProcessamentoService.Processamento {
    
    private Long notificacao;
    private FiltroDispositivoNotificacaoDTO filtro;
    private MensagemPushDTO t;
    
    @Override
    public String getId() {
        return notificacao.toString();
    }
    
    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        filtro.setTipo(TipoDispositivo.IPHONE);
        filtro.setPagina(tool.getStep());
            
        BuscaPaginadaDTO<Object[]> dispositivos = tool.getDaoService().findWith(new FiltroDispositivoNotificacao(filtro));

        if (!dispositivos.isEmpty()){
            ((IOSNotificationService) tool.getSessionContext().lookup("ejb/IOSNotificationService")).
                    pushNotifications(filtro.getIgreja(), t, dispositivos.getResultados());

            tool.getDaoService().execute(new RegisterSentNotifications(notificacao, filtro));
        }else{
            Logger.getLogger(MensagemServiceImpl.class.getName()).warning("Nenhum dispositivo iOS para notificação " + t);
        }

        return dispositivos.getTotalPaginas();
    }
    
    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {}
    
    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {}
    
}
