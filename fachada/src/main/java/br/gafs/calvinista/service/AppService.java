/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.*;
import br.gafs.calvinista.entity.*;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.TipoVersao;
import br.gafs.dao.BuscaPaginadaDTO;
import java.io.File;

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
    
    Cifra cadastra(Cifra cifra) throws IOException;
    Cifra atualiza(Cifra cifra) throws IOException;
    void removeCifra(Long cifra);
    BuscaPaginadaDTO<Cifra> busca(FiltroCifraDTO filtro);
    Cifra buscaCifra(Long cifra);
    String extraiTexto(Long idArquivo);
    
    Chamado solicita(Chamado chamado);
    Chamado buscaChamado(Long chamado);
    BuscaPaginadaDTO<Chamado> busca(FiltroChamadoDTO filtro);

    Membro cadastra(Membro membro);
    Membro atualiza(Membro membro);
    void removeMembro(Long membro);
    void redefinirSenha(Long membro);
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
    
    BuscaPaginadaDTO<LivroBiblia> busca(FiltroLivroBibliaDTO filtro);
    
    Institucional recuperaInstitucional();
    Institucional atualiza(Institucional institucional);

    List<CategoriaEstudo> buscaCategoriasEstudo();
    CategoriaEstudo cadastra(CategoriaEstudo categoria);
    Estudo cadastra(Estudo estudo);
    Estudo atualiza(Estudo estudo);
    void removeEstudo(Long estudo);
    BuscaPaginadaDTO<Estudo> buscaTodos(FiltroEstudoDTO filtro);
    BuscaPaginadaDTO<Estudo> buscaPublicados(FiltroEstudoPublicadoDTO filtro);
    Estudo buscaEstudo(Long estudo);

    List<CategoriaAudio> buscaCategoriasAudio();
    CategoriaAudio cadastra(CategoriaAudio categoria);
    Audio cadastra(Audio estudo);
    Audio atualiza(Audio estudo);
    void removeAudio(Long audio);
    BuscaPaginadaDTO<Audio> buscaTodos(FiltroAudioDTO filtro);
    Audio buscaAudio(Long audio);

    BuscaPaginadaDTO<GaleriaDTO> buscaGaleriasFotos(Integer pagina);
    BuscaPaginadaDTO<FotoDTO> buscaFotos(FiltroFotoDTO filtro);
    String buscaURLAutenticacaoFlickr() throws IOException;
    void iniciaConfiguracaoFlickr(String token, String verifier);
    void desvinculaFlickr();

    Noticia cadastra(Noticia noticia);
    Noticia atualiza(Noticia noticia);
    void removeNoticia(Long noticia);
    BuscaPaginadaDTO<Noticia> buscaTodos(FiltroNoticiaDTO filtro);
    BuscaPaginadaDTO<Noticia> buscaPublicados(FiltroNoticiaPublicadaDTO filtro);
    Noticia buscaNoticia(Long noticia);
    
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
    ResultadoInscricaoDTO realizaInscricao(List<InscricaoEvento> merged);
    void confirmaInscricao(Long evento, Long inscricao);
    void cancelaInscricao(Long evento, Long inscricao);
    
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
    
    List<ReleaseNotes> buscaReleaseNotes(TipoVersao tipo);
    
    void verificaPagSeguroPorCodigo(String code);
    
    void verificaPagSeguroPorIdTransacao(String transactionId);
    
    File buscaAjuda(String path);
    
    void clearNotificacoes(List<Long> excecoes);
    void removeNotificacao(Long notificacao);
    BuscaPaginadaDTO<NotificationSchedule> buscaNotificacoes(FiltroNotificacoesDTO filtro);

    ConfiguracaoIgrejaDTO atualiza(ConfiguracaoIgrejaDTO configuracao);

    String buscaURLAutenticacaoYouTube() throws IOException;
    void iniciaConfiguracaoYouTube(String code);
    List<VideoDTO> buscaVideos();
    ConfiguracaoYouTubeIgrejaDTO buscaConfiguracaoYouTube();
    void desvinculaYouTube();
    ConfiguracaoIgrejaDTO buscaConfiguracao();
    ConfiguracaoYouTubeIgrejaDTO atualiza(ConfiguracaoYouTubeIgrejaDTO configuracao);

    String buscaURLAutenticacaoCalendar() throws IOException;
    void iniciaConfiguracaoCalendar(String code);
    BuscaPaginadaEventosCalendarioDTO buscaEventos(String pagina, Integer total);
    ConfiguracaoCalendarIgrejaDTO buscaConfiguracaoCalendar();
    void desvinculaCalendar();
    ConfiguracaoCalendarIgrejaDTO atualiza(ConfiguracaoCalendarIgrejaDTO configuracao);
    List<CalendarioGoogleDTO> buscaVisoesCalendar() throws IOException;

    BuscaPaginadaDTO<PlanoLeituraBiblica> buscaTodos(FiltroPlanoLeituraBiblicaDTO filtro);
    PlanoLeituraBiblica buscaPlanoLeitura(Long idPlano);
    PlanoLeituraBiblica cadastra(PlanoLeituraBiblica plano);

    PlanoLeituraBiblica atualiza(PlanoLeituraBiblica plano);

    void removePlanoLeitura(Long idPlano);

    BuscaPaginadaDTO<LeituraBibliaDTO> selecionaPlano(Long plano);

    void desselecionaPlano();

    LeituraBibliaDTO marcaLeitura(Long dia);
    LeituraBibliaDTO desmarcaLeitura(Long dia);

    PlanoLeituraBiblica buscaPlanoSelecionado();

    BuscaPaginadaDTO<LeituraBibliaDTO> buscaPlanoSelecionado(Date ultimaAlteracao, int pagina, int total);

    List<Membro> buscaProximosAniversariantes();
}
