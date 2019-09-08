/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.service;

import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.entity.*;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.TipoVersao;

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

    BuscaPaginadaDTO<ResumoEmpresaDTO> busca(FiltroEmpresaDTO filtro);
    Template buscaTemplate();
    TemplateAplicativo buscaTemplateApp();
    StatusAdminDTO buscaStatus();
    
    Perfil cadastra(Perfil perfil);
    Perfil atualiza(Perfil perfil);
    void removePerfil(Long perfil);
    List<Perfil> buscaPerfis();
    Perfil buscaPerfil(Long perfil);

    Chamado solicita(Chamado chamado);
    Chamado buscaChamado(Long chamado);
    BuscaPaginadaDTO<Chamado> busca(FiltroChamadoDTO filtro);

    List<LotacaoColaborador> buscaLotacoesColaborador();
    LotacaoColaborador cadastra(LotacaoColaborador categoria);
    Colaborador cadastra(Colaborador colaborador);
    Colaborador atualiza(Colaborador colaborador);
    void removeColaborador(Long colaborador);
    void redefinirSenha(Long colaborador);
    BuscaPaginadaDTO<Colaborador> busca(FiltroColaboradorDTO filtro);
    Colaborador buscaColaborador(Long colaborador);
    
    BoletimInformativo cadastra(BoletimInformativo boletimInformativo) throws IOException;
    BoletimInformativo atualiza(BoletimInformativo boletimInformativo) throws IOException;
    void removeBoletim(Long boletim);
    BuscaPaginadaDTO<BoletimInformativo> buscaTodos(FiltroBoletimDTO filtro);
    BuscaPaginadaDTO<BoletimInformativo> buscaPublicados(FiltroBoletimPublicadoDTO filtro);
    BoletimInformativo buscaBoletim(Long boletim);
    
    Institucional recuperaInstitucional();
    Institucional atualiza(Institucional institucional);

    List<CategoriaDocumento> buscaCategoriasDocumento();
    CategoriaDocumento cadastra(CategoriaDocumento categoria);
    Documento cadastra(Documento documento);
    Documento atualiza(Documento documento);
    void removeDocumento(Long documento);
    BuscaPaginadaDTO<Documento> buscaTodos(FiltroDocumentoDTO filtro);
    BuscaPaginadaDTO<Documento> buscaPublicados(FiltroDocumentoPublicadoDTO filtro);
    Documento buscaDocumento(Long documento);

    List<CategoriaAudio> buscaCategoriasAudio();
    CategoriaAudio cadastra(CategoriaAudio categoria);
    Audio cadastra(Audio documento);
    Audio atualiza(Audio documento);
    void removeAudio(Long audio);
    BuscaPaginadaDTO buscaTodos(FiltroAudioDTO filtro);
    Audio buscaAudio(Long audio);

    BuscaPaginadaDTO<GaleriaDTO> buscaGaleriasFotos(Integer pagina);
    BuscaPaginadaDTO<FotoDTO> buscaFotos(FiltroFotoDTO filtro);
    ConfiguracaoFlickrEmpresaDTO buscaConfiguracaoFlickr();
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
    
    Enquete cadastra(Enquete enquete);
    Enquete atualiza(Enquete enquete);
    void removeEnquete(Long enquete);
    BuscaPaginadaDTO<Enquete> buscaTodas(FiltroEnqueteDTO filtro);
    BuscaPaginadaDTO<Enquete> buscaAtivas(FiltroEnqueteAtivaDTO filtro);
    Enquete buscaEnquete(Long enquete);
    ResultadoEnqueteDTO buscaResultado(Long enquete);
    void realizarEnquete(RespostaEnquete resposta);
    
    ContatoColaborador realizaContato(ContatoColaborador contato);
    ContatoColaborador atende(Long contatoColaborador);
    BuscaPaginadaDTO<ContatoColaborador> buscaTodos(FiltroContatoColaboradorDTO filtro);
    BuscaPaginadaDTO<ContatoColaborador> buscaMeus(FiltroMeusContatosColaboradorDTO filtro);
    
    AgendamentoAtendimento agenda(Long colaborador, Long horario, Date data);
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
    
    MensagemDia cadastra(MensagemDia mensagemDia);
    MensagemDia habilita(Long mensagemDia);
    MensagemDia desabilita(Long mensagemDia);
    void removeMensagemDia(Long mensagemDia);
    BuscaPaginadaDTO<MensagemDia> busca(FiltroMensagemDiaDTO filtro);
    MensagemDia buscaMensagemDia(Long mensagemDia);
    
    Empresa buscaPorChave(String propriedade);
    
    Colaborador darAcessoColaborador(Long colaborador);
    Colaborador retiraAcessoColaborador(Long colaborador);
    Acesso buscaAcessoAdmin(Long colaborador);
    Acesso darAcessoAdmin(Long colaborador, List<Perfil> perfis);
    void retiraAcessoAdmin(Long colaborador);
    
    List<Colaborador> buscaGerentes();
    
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

    ConfiguracaoEmpresaDTO atualiza(ConfiguracaoEmpresaDTO configuracao);

    String buscaURLAutenticacaoYouTube() throws IOException;
    void iniciaConfiguracaoYouTube(String code);
    List<VideoDTO> buscaVideos();
    ConfiguracaoYouTubeEmpresaDTO buscaConfiguracaoYouTube();
    void desvinculaYouTube();
    ConfiguracaoEmpresaDTO buscaConfiguracao();
    ConfiguracaoYouTubeEmpresaDTO atualiza(ConfiguracaoYouTubeEmpresaDTO configuracao);

    String buscaURLAutenticacaoCalendar() throws IOException;
    void iniciaConfiguracaoCalendar(String code);
    BuscaPaginadaEventosCalendarioDTO buscaEventos(Integer pagina, Integer total);
    ConfiguracaoCalendarEmpresaDTO buscaConfiguracaoCalendar();
    void desvinculaCalendar();
    ConfiguracaoCalendarEmpresaDTO atualiza(ConfiguracaoCalendarEmpresaDTO configuracao);
    List<CalendarioGoogleDTO> buscaVisoesCalendar() throws IOException;

    List<Colaborador> buscaProximosAniversariantes();

    List<QuantidadeDispositivoDTO> buscaQuantidadesDispositivos();
    List<EstatisticaDispositivo> buscaEstatisticasDispositivos();
    List<EstatisticaAcesso> buscaEstatisticasAcessoFuncionalidade(Funcionalidade funcionalidade);
}
