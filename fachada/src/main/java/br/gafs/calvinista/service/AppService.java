/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.EventoAgendaDTO;
import br.gafs.calvinista.dto.FiltroBoletimDTO;
import br.gafs.calvinista.dto.FiltroBoletimPublicadoDTO;
import br.gafs.calvinista.dto.FiltroEstudoDTO;
import br.gafs.calvinista.dto.FiltroEstudoPublicadoDTO;
import br.gafs.calvinista.dto.FiltroEventoDTO;
import br.gafs.calvinista.dto.FiltroEventoFuturoDTO;
import br.gafs.calvinista.dto.FiltroHinoDTO;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.dto.FiltroMembroDTO;
import br.gafs.calvinista.dto.FiltroMeusAgendamentoDTO;
import br.gafs.calvinista.dto.FiltroMeusPedidoOracaoDTO;
import br.gafs.calvinista.dto.FiltroMinhasInscricoesDTO;
import br.gafs.calvinista.dto.FiltroPedidoOracaoDTO;
import br.gafs.calvinista.dto.FiltroVersiculoDiarioDTO;
import br.gafs.calvinista.dto.FiltroVotacaoAtivaDTO;
import br.gafs.calvinista.dto.FiltroVotacaoDTO;
import br.gafs.calvinista.dto.ResultadoVotacaoDTO;
import br.gafs.calvinista.dto.StatusAdminDTO;
import br.gafs.calvinista.entity.Acesso;
import br.gafs.calvinista.entity.AgendamentoAtendimento;
import br.gafs.calvinista.entity.Boletim;
import br.gafs.calvinista.entity.CalendarioAtendimento;
import br.gafs.calvinista.entity.Estudo;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.Hino;
import br.gafs.calvinista.entity.HorarioAtendimento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.Institucional;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Notificacao;
import br.gafs.calvinista.entity.Opcao;
import br.gafs.calvinista.entity.PedidoOracao;
import br.gafs.calvinista.entity.Perfil;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.RespostaVotacao;
import br.gafs.calvinista.entity.VersiculoDiario;
import br.gafs.calvinista.entity.Votacao;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.dao.BuscaPaginadaDTO;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Gabriel
 */
public interface AppService extends Serializable {
    
    StatusAdminDTO buscaStatus();
    
    Ministerio cadastra(Ministerio grupo);
    Ministerio atualiza(Ministerio grupo);
    void removeMinisterio(Long ministerio);
    List<Ministerio> buscaMinisterios();
    Ministerio buscaMinisterio(Long Ministerio);
    
    Perfil cadastra(Perfil perfil);
    Perfil atualiza(Perfil perfil);
    void removePerfil(Long perfil);
    List<Perfil> buscaPerfis();
    Perfil buscaPerfil(Long perfil);
    
    Membro cadastra(Membro membro);
    Membro atualiza(Membro membro);
    void removeMembro(Long membro);
    BuscaPaginadaDTO<Membro> busca(FiltroMembroDTO filtro);
    Membro buscaMembro(Long membro);
    
    Boletim cadastra(Boletim boletim) throws IOException;
    Boletim atualiza(Boletim boletim) throws IOException;
    void removeBoletim(Long boletim);
    BuscaPaginadaDTO<Boletim> buscaTodos(FiltroBoletimDTO filtro);
    BuscaPaginadaDTO<Boletim> buscaPublicados(FiltroBoletimPublicadoDTO filtro);
    Boletim buscaBoletim(Long boletim);
    
    BuscaPaginadaDTO<Hino> busca(FiltroHinoDTO filtro);
    Hino buscaHino(Long hino);
    
    Institucional recuperaInstitucional();
    Institucional atualiza(Institucional institucional);
    
    Estudo cadastra(Estudo estudo);
    Estudo atualiza(Estudo estudo);
    void removeEstudo(Long estudo);
    BuscaPaginadaDTO<Estudo> buscaTodos(FiltroEstudoDTO filtro);
    BuscaPaginadaDTO<Estudo> buscaPublicados(FiltroEstudoPublicadoDTO filtro);
    Estudo buscaEstudo(Long estudo);
    
    void enviar(Notificacao notificacao);
    
    Votacao cadastra(Votacao votacao);
    Votacao atualiza(Votacao votacao);
    void removeVotacao(Long votacao);
    BuscaPaginadaDTO<Votacao> buscaTodas(FiltroVotacaoDTO filtro);
    BuscaPaginadaDTO<Votacao> buscaAtivas(FiltroVotacaoAtivaDTO filtro);
    Votacao buscaVotacao(Long votacao);
    ResultadoVotacaoDTO buscaResultado(Long votacao);
    void realizarVotacao(RespostaVotacao resposta);
    
    PedidoOracao realizaPedido(PedidoOracao pedido);
    PedidoOracao atende(Long pedidoOracao);
    BuscaPaginadaDTO<PedidoOracao> buscaTodos(FiltroPedidoOracaoDTO filtro);
    BuscaPaginadaDTO<PedidoOracao> buscaMeus(FiltroMeusPedidoOracaoDTO filtro);
    
    AgendamentoAtendimento agenda(Long membro, Long horario, Date data);
    AgendamentoAtendimento confirma(Long agendamento);
    AgendamentoAtendimento cancela(Long agendamento);
    List<AgendamentoAtendimento> buscaAgendamentos(CalendarioAtendimento calendario, Date dataInicio, Date dataTermino);
    AgendamentoAtendimento buscaAgendamento(Long agendamento);
    BuscaPaginadaDTO<AgendamentoAtendimento> buscaMeusAgendamentos(FiltroMeusAgendamentoDTO filtro);
    
    CalendarioAtendimento cadastra(CalendarioAtendimento calendario);
    void removeCalendario(Long calendario);
    List<CalendarioAtendimento> buscaCalendarios();
    CalendarioAtendimento buscaCalendario(Long calendario);
    
    List<EventoAgendaDTO> buscaAgenda(Long idCalendario, Date dataInicio, Date dataTermino);
    void cadastra(Long idCalendario, HorarioAtendimento horario);
    void removeDia(Long calendario, Long horario, Date data);
    void removePeriodo(Long calendario, Long horario, Date inicio, Date termino);
    
    Evento cadastra(Evento evento);
    Evento atualiza(Evento evento);
    void removeEvento(Long evento);
    BuscaPaginadaDTO<Evento> buscaTodos(FiltroEventoDTO filtro);
    BuscaPaginadaDTO<Evento> buscaFuturos(FiltroEventoFuturoDTO filtro);
    BuscaPaginadaDTO<InscricaoEvento> buscaTodas(Long evento, FiltroInscricaoDTO filtro);
    BuscaPaginadaDTO<InscricaoEvento> buscaMinhas(Long evento, FiltroMinhasInscricoesDTO filtro);
    Evento buscaEvento(Long evento);
    String realizaInscricao(List<InscricaoEvento> merged);
    void confirmaInscricao(Long evento, Long inscricao);
    
    VersiculoDiario cadastra(VersiculoDiario versiculoDiario);
    VersiculoDiario habilita(Long versiculo);
    VersiculoDiario desabilita(Long versiculo);
    void removeVersiculo(Long versiculoDiario);
    BuscaPaginadaDTO<VersiculoDiario> busca(FiltroVersiculoDiarioDTO filtro);
    VersiculoDiario buscaVersiculo(Long versiculoDiario);

    Igreja buscaPorChave(String propriedade);

    List<Ministerio> buscaMinisteriosPorAcesso();

    Membro darAcessoMembro(Long membro);
    Membro retiraAcessoMembro(Long membro);
    Acesso buscaAcessoAdmin(Long membro);
    Acesso darAcessoAdmin(Long membro, List<Perfil> perfis, List<Ministerio> ministerios);
    void retiraAcessoAdmin(Long membro);

    List<Membro> buscaPastores();

    Questao buscaQuestao(Long id);

    Opcao buscaOpcao(Long id);

    List<Funcionalidade> getFuncionalidadesHabilitadasAplicativo();

    void salvaFuncionalidadesHabilitadasAplicativo(List<Funcionalidade> funcionalidades);

    List<Funcionalidade> getFuncionalidadesAplicativo();

    
}
