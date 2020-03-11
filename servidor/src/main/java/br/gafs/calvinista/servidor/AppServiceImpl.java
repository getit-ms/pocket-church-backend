/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dao.*;
import br.gafs.calvinista.dto.*;
import br.gafs.calvinista.entity.*;
import br.gafs.calvinista.entity.domain.*;
import br.gafs.calvinista.exception.ValidationException;
import br.gafs.calvinista.security.*;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.ArquivoService;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.batch.BatchService;
import br.gafs.calvinista.servidor.facebook.FacebookService;
import br.gafs.calvinista.servidor.flickr.FlickrService;
import br.gafs.calvinista.servidor.google.GoogleService;
import br.gafs.calvinista.servidor.pagseguro.PagSeguroService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.servidor.relatorio.RelatorioEstudo;
import br.gafs.calvinista.servidor.relatorio.RelatorioInscritos;
import br.gafs.calvinista.util.MensagemBuilder;
import br.gafs.calvinista.util.Persister;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.dao.QueryParameters;
import br.gafs.exceptions.ServiceException;
import br.gafs.file.EntityFileManager;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;
import com.mpatric.mp3agic.Mp3File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gabriel
 */
@Stateless
@Local(AppService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class, SecurityInterceptor.class})
public class AppServiceImpl implements AppService {

    public static final Logger LOGGER = Logger.getLogger(AppServiceImpl.class.getName());
    private static final Integer HORA_MINIMA_NOTIFICACAO = 8;
    private static final Integer HORA_MAXIMA_NOTIFICACAO = 22;

    @EJB
    private DAOService daoService;

    @EJB
    private ArquivoService arquivoService;

    @EJB
    private BatchService batchServie;

    @EJB
    private MensagemService notificacaoService;

    @EJB
    private PagSeguroService pagSeguroService;

    @EJB
    private GoogleService googleService;

    @EJB
    private FacebookService facebookService;

    @EJB
    private ParametroService paramService;

    @EJB
    private ProcessamentoService processamentoService;

    @EJB
    private FlickrService flickrService;

    @EJB
    private MensagemBuilder mensagemBuilder;

    @EJB
    private DispositivoService dispositivoService;

    @Inject
    private SessaoBean sessaoBean;

    @Override
    public BuscaPaginadaDTO<ResumoIgrejaDTO> busca(FiltroIgrejaDTO filtro) {
        return daoService.findWith(new FiltroIgreja(filtro));
    }

    @Override
    public Template buscaTemplate() {
        Template template = daoService.find(Template.class, sessaoBean.getChaveIgreja());
        if (template == null) {
            template = new Template(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        }
        return template;
    }

    @Override
    public TemplateAplicativo buscaTemplateApp() {
        TemplateAplicativo template = daoService.find(TemplateAplicativo.class, sessaoBean.getChaveIgreja());
        if (template == null) {
            template = new TemplateAplicativo(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        }
        return template;
    }

    @Override
    @AllowAdmin
    public StatusAdminDTO buscaStatus() {
        StatusAdminDTO status = new StatusAdminDTO();
        status.setVersiculoDiario(buscaVersiculoDiario());

        if (sessaoBean.temPermissao(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)) {
            Number pedidos = daoService.findWith(new FiltroPedidoOracao(null, sessaoBean.getChaveIgreja(),
                    new FiltroPedidoOracaoDTO(null, null, Arrays.asList(StatusPedidoOracao.PENDENTE), 1, 1)).getCountQuery());

            if (pedidos.intValue() > 0) {
                status.addNotificacao("mensagens.MSG-036", pedidos.intValue(),
                        new QueryParameters("quantidade", pedidos),
                        "/oracao");
            }
        }

        boolean temPermissaoAcessoMembros = sessaoBean.temPermissao(Funcionalidade.GERENCIAR_ACESSO_MEMBROS);
        boolean temPermissaoManterMemebros = sessaoBean.temPermissao(Funcionalidade.MANTER_MEMBROS);
        if (temPermissaoAcessoMembros || temPermissaoManterMemebros) {

            Number pendentes = daoService.findWith(new FiltroMembro(true, sessaoBean.getChaveIgreja(),
                    new FiltroMembroDTO(null, null, null, 1, 1, null, true)).getCountQuery());

            if (pendentes.intValue() > 0) {
                status.addNotificacao("mensagens.MSG-058", pendentes.intValue(),
                        new QueryParameters("quantidade", pendentes),
                        temPermissaoAcessoMembros ? "/contato?pendentes=true" : "/membro?pendentes=true");
            }
        }

        if (sessaoBean.temPermissao(Funcionalidade.MANTER_EVENTOS)) {
            Number pendentes = daoService.findWith(new FiltroInscricao(null, sessaoBean.getChaveIgreja(), null,
                    new FiltroInscricaoDTO(TipoEvento.EVENTO, Collections.singletonList(StatusInscricaoEvento.PENDENTE), 1, 1)).getCountQuery());

            if (pendentes.intValue() > 0) {
                status.addNotificacao("mensagens.MSG-059", pendentes.intValue(),
                        new QueryParameters("quantidade", pendentes),
                        "/evento");
            }
        }

        if (sessaoBean.temPermissao(Funcionalidade.MANTER_EBD)) {
            Number pendentes = daoService.findWith(new FiltroInscricao(null, sessaoBean.getChaveIgreja(), null,
                    new FiltroInscricaoDTO(TipoEvento.EBD, Collections.singletonList(StatusInscricaoEvento.PENDENTE), 1, 1)).getCountQuery());

            if (pendentes.intValue() > 0) {
                status.addNotificacao("mensagens.MSG-060", pendentes.intValue(),
                        new QueryParameters("quantidade", pendentes),
                        "/ebd");
            }
        }

        return status;
    }

    @Override
    public BuscaPaginadaDTO<NotificationSchedule> buscaNotificacoes(FiltroNotificacoesDTO filtro) {
        BuscaPaginadaDTO<NotificationSchedule> busca = daoService.findWith(new FiltroNotificacoes(sessaoBean.getChaveIgreja(),
                sessaoBean.getChaveDispositivo(), sessaoBean.getIdMembro(), filtro));

        if (filtro.getPagina().equals(1)) {
            notificacaoService.marcaNotificacoesComoLidas(
                    sessaoBean.getChaveIgreja(),
                    sessaoBean.getChaveDispositivo(),
                    sessaoBean.getIdMembro()
            );
        }

        return busca;
    }

    @Override
    public void clearNotificacoes(List<Long> excecoes) {
        if (excecoes == null || excecoes.isEmpty()) {
            // Evita erros de SQL por causa de lista vazia
            excecoes = Arrays.asList(-1L);
        }

        if (sessaoBean.getIdMembro() != null) {
            daoService.execute(QueryNotificacao.CLEAR_NOTIFICACOES_MEMBRO.
                    create(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), excecoes));
        }

        daoService.execute(QueryNotificacao.CLEAR_NOTIFICACOES_DISPOSITIVO.
                create(sessaoBean.getChaveIgreja(), sessaoBean.getChaveDispositivo(), excecoes));
    }

    @Override
    public void removeNotificacao(Long notificacao) {
        SentNotification sn = daoService.find(SentNotification.class, new SentNotificationId(sessaoBean.getChaveDispositivo(), notificacao));

        if (sn != null && (sn.getMembro() == null || sn.getMembro().equals(sessaoBean.getIdMembro()))) {
            daoService.delete(SentNotification.class, sn.getId());
        }

        if (sessaoBean.getIdMembro() != null) {
            List<SentNotification> sns = daoService.findWith(QueryNotificacao.NOTIFICACAO_MEMBRO.
                    create(notificacao, sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));

            for (SentNotification sn0 : sns) {
                daoService.delete(SentNotification.class, sn0.getId());
            }
        }
    }

    @Override
    @AllowAdmin
    @AllowMembro
    public List<ReleaseNotes> buscaReleaseNotes(TipoVersao tipo) {
        return daoService.findWith(QueryAdmin.RELEASE_NOTES.create(tipo));
    }

    private VersiculoDiario buscaVersiculoDiario() {
        return daoService.findWith(QueryAdmin.VERSICULOS_POR_STATUS.
                createSingle(sessaoBean.getChaveIgreja(), StatusVersiculoDiario.ATIVO));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public List<Membro> buscaPastores() {
        return daoService.findWith(QueryAdmin.PASTORES_ATIVOS.
                create(sessaoBean.getChaveIgreja()));
    }

    @Override
    public Chamado solicita(Chamado chamado) {
        if (sessaoBean.isAdmin()) {
            chamado.setTipo(TipoChamado.SUPORTE);

            if (sessaoBean.getIdMembro() == null ||
                    !sessaoBean.temPermissao(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)) {
                throw new ServiceException("mensagens.MSG-403");
            }
        } else if (chamado.isSuporte()) {
            throw new ServiceException("mensagens.MSG-403");
        }

        chamado.setDispositivoSolicitante(dispositivoService.getDispositivo(sessaoBean.getChaveDispositivo()));
        chamado = daoService.create(chamado);

        EmailUtil.sendMail(
                MessageFormat.format(ResourceBundleUtil._default().getPropriedade("CHAMADO_MESSAGE"),
                        chamado.getDescricao(), chamado.getIgrejaSolicitante().getNome(),
                        chamado.getNomeSolicitante(), chamado.getEmailSolicitante(),
                        chamado.getDispositivoSolicitante().getUuid(),
                        chamado.getDispositivoSolicitante().getVersao()),
                MessageFormat.format(ResourceBundleUtil._default().getPropriedade("CHAMADO_SUBJECT"),
                        chamado.getIgrejaSolicitante().getChave().toUpperCase(),
                        chamado.getCodigo(), chamado.getTipo().name()),
                ResourceBundleUtil._default().getPropriedade("CHAMADO_MAIL").split("\\s*,\\s*"));

        if (chamado.getDescricao().startsWith("Chamado automático")) {
            daoService.delete(Chamado.class, chamado.getId());
        }

        return chamado;
    }

    @Override
    @AllowAdmin(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)
    public Chamado buscaChamado(Long id) {
        Chamado chamado = daoService.find(Chamado.class, id);
        if (chamado == null || !chamado.getIgrejaSolicitante().getChave().equals(sessaoBean.getChaveIgreja()) ||
                (chamado.isSuporte() && (sessaoBean.getIdMembro() == null ||
                        !sessaoBean.temPermissao(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)))) {
            throw new ServiceException("mensagens.MSG-403");
        }
        return chamado;
    }

    @Override
    public BuscaPaginadaDTO<Chamado> busca(FiltroChamadoDTO filtro) {
        return daoService.findWith(new FiltroChamado(sessaoBean.getChaveIgreja(),
                sessaoBean.getChaveDispositivo(), sessaoBean.getIdMembro(), sessaoBean.isAdmin(), filtro));
    }

    @Audit
    @Override
    public Membro cadastra(Membro membro) {
        if (sessaoBean.isAdmin() && sessaoBean.temPermissao(Funcionalidade.MANTER_MEMBROS)) {
            membro.ativo();
        }

        membro.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));

        if (membro.getFoto() != null) {
            arquivoService.registraUso(membro.getFoto().getId());
            membro.setFoto(arquivoService.buscaArquivo(membro.getFoto().getId()));
        }

        return daoService.create(membro);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public Membro darAcessoMembro(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (entidade.getId().equals(sessaoBean.getIdMembro())) {
            throw new ServiceException("mensagens.MSG-015");
        }

        entidade = trataEmailsReplicados(entidade);

        boolean gerarSenha = entidade.isSenhaUndefined();

        entidade.membro();
        entidade = daoService.update(entidade);

        if (gerarSenha) {
            String senha = SenhaUtil.geraSenha(8);

            entidade.setSenha(SenhaUtil.encryptSHA256(senha));

            entidade = daoService.update(entidade);

            notificacaoService.sendNow(
                    mensagemBuilder.email(
                            entidade.getIgreja(),
                            TipoParametro.EMAIL_SUBJECT_DAR_ACESSO,
                            TipoParametro.EMAIL_BODY_DAR_ACESSO,
                            entidade.getNome(), senha
                    ),
                    new FiltroEmailDTO(entidade.getIgreja(), entidade.getId()));
        }

        return entidade;
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public List<Funcionalidade> getFuncionalidadesHabilitadasAplicativo() {
        return daoService.find(Igreja.class, sessaoBean.getChaveIgreja()).getFuncionalidadesAplicativo();
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public List<Funcionalidade> getFuncionalidadesAplicativo() {
        return daoService.find(Igreja.class, sessaoBean.getChaveIgreja()).getPlano().getFuncionalidadesMembro();
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public void salvaFuncionalidadesHabilitadasAplicativo(List<Funcionalidade> funcionalidades) {
        Igreja igreja = daoService.find(Igreja.class, sessaoBean.getChaveIgreja());
        igreja.setFuncionalidadesAplicativo(funcionalidades);
        daoService.update(igreja);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public Membro retiraAcessoMembro(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (entidade.getId().equals(sessaoBean.getIdMembro())) {
            throw new ServiceException("mensagens.MSG-015");
        }

        entidade.contato();
        return daoService.update(entidade);
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public Acesso buscaAcessoAdmin(Long membro) {
        return daoService.find(Acesso.class, new AcessoId(new RegistroIgrejaId(
                sessaoBean.getChaveIgreja(), membro)));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public void removeMembro(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (!entidade.isMembro()) {
            entidade.exclui();
            daoService.update(entidade);
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public void redefinirSenha(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (entidade.isMembro()) {
            String senha = SenhaUtil.geraSenha(8);

            entidade.setSenha(SenhaUtil.encryptSHA256(senha));
            daoService.update(entidade);

            notificacaoService.sendNow(
                    mensagemBuilder.email(
                            entidade.getIgreja(),
                            TipoParametro.EMAIL_SUBJECT_REDEFINIR_SENHA,
                            TipoParametro.EMAIL_BODY_REDEFINIR_SENHA,
                            entidade.getNome(), senha
                    ),
                    new FiltroEmailDTO(entidade.getIgreja(), entidade.getId()));
        }
    }

    @Override
    public BuscaPaginadaDTO<Hino> busca(FiltroHinoDTO filtro) {
        return daoService.findWith(new FiltroHino(sessaoBean.getChaveIgreja(), filtro));
    }

    @Override
    public Hino buscaHino(Long hino) {
        return daoService.find(Hino.class, hino);
    }

    @Override
    public BuscaPaginadaDTO<LivroBiblia> busca(FiltroLivroBibliaDTO filtro) {
        if (DispositivoService.shouldResetaBiblia(sessaoBean.getChaveDispositivo())) {
            filtro.setUltimaAtualizacao(null);
        }

        BuscaPaginadaDTO pagina = daoService.findWith(new FiltroLivroBiblia(sessaoBean.getChaveIgreja(), filtro));

        if (!pagina.isHasProxima()) {
            DispositivoService.flagResetBibliaConcluido(sessaoBean.getChaveDispositivo());
        }

        return pagina;
    }

    @Override
    public Opcao buscaOpcao(Long id) {
        return daoService.find(Opcao.class, id);
    }

    @Override
    public Questao buscaQuestao(Long id) {
        return daoService.find(Questao.class, id);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public Acesso darAcessoAdmin(Long membro, List<Perfil> perfis, List<Ministerio> ministerios) {
        Membro entidade = buscaMembro(membro);

        Acesso acesso = buscaAcessoAdmin(membro);
        if (acesso == null) {
            if (!entidade.isMembro()) {
                entidade = darAcessoMembro(membro);
            }

            acesso = daoService.create(new Acesso(entidade));
        }

        acesso.setPerfis(perfis);
        acesso.setMinisterios(ministerios);

        if (!acesso.possuiPermissao(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)) {
            if (entidade.getId().equals(sessaoBean.getIdMembro())) {
                throw new ServiceException("mensagens.MSG-015");
            }
        }

        return daoService.update(acesso);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTEM_DEVOCIONARIO)
    public BuscaPaginadaDTO<DiaDevocionario> buscaTodos(FiltroDevocionarioDTO filtro) {
        return daoService.findWith(new FiltroDevocionario(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<DiaDevocionario> buscaPublicados(FiltroDevocionarioPublicadoDTO filtro) {
        AcessoMarkerContext.funcionalidade(Funcionalidade.DEVOCIONARIO);

        return buscaTodos(filtro);
    }

    @Override
    public DiaDevocionario buscaDiaDevocionario(Long id) {
        return daoService.find(DiaDevocionario.class,
                new RegistroIgrejaId(sessaoBean.getChaveIgreja(), id));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTEM_DEVOCIONARIO)
    public DiaDevocionario cadastra(DiaDevocionario diaDevocionario) {
        diaDevocionario.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        diaDevocionario.setArquivo(arquivoService.buscaArquivo(diaDevocionario.getArquivo().getId()));

        boolean trocouPDF = trataTrocaPDF(diaDevocionario);

        if (trocouPDF) {
            diaDevocionario.processando();
        }

        DiaDevocionario salvo = daoService.create(diaDevocionario);

        if (trocouPDF) {
            batchServie.processaDevocional(diaDevocionario.getChaveIgreja(), salvo.getId());
        }

        return salvo;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTEM_DEVOCIONARIO)
    public DiaDevocionario atualiza(DiaDevocionario diaDevocionario) {
        diaDevocionario.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        diaDevocionario.setArquivo(arquivoService.buscaArquivo(diaDevocionario.getArquivo().getId()));
        diaDevocionario.setUltimaAlteracao(DateUtil.getDataAtual());

        boolean trocouPDF = trataTrocaPDF(diaDevocionario);

        if (trocouPDF) {
            diaDevocionario.processando();
        }

        DiaDevocionario salvo = daoService.update(diaDevocionario);

        if (trocouPDF) {
            batchServie.processaDevocional(diaDevocionario.getChaveIgreja(), salvo.getId());
        }

        return salvo;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTEM_DEVOCIONARIO)
    public void removeDiaDevocionario(Long dia) {
        DiaDevocionario entidade = buscaDiaDevocionario(dia);

        if (entidade != null) {
            arquivoService.registraDesuso(entidade.getArquivo().getId());

            if (entidade.getThumbnail() != null) {
                arquivoService.registraDesuso(entidade.getThumbnail().getId());
            }

            daoService.delete(DiaDevocionario.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public void retiraAcessoAdmin(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (entidade.getId().equals(sessaoBean.getIdMembro())) {
            throw new ServiceException("mensagens.MSG-015");
        }

        entidade.retiraAdmin();
        entidade.membro();
        daoService.update(entidade);
    }

    @Override
    @AllowAdmin({
            Funcionalidade.GERENCIAR_ACESSO_MEMBROS,
            Funcionalidade.ENVIAR_NOTIFICACOES
    })
    public List<Ministerio> buscaMinisteriosPorAcesso() {
        return daoService.findWith(QueryAdmin.MINISTERIOS_POR_ACESSO.create(
                sessaoBean.getChaveIgreja(),
                sessaoBean.getIdMembro()));
    }

    @Override
    public Igreja buscaPorChave(String chave) {
        return daoService.find(Igreja.class, chave);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Membro atualiza(Membro membro) {
        if (membro.getFoto() != null) {
            arquivoService.registraUso(membro.getFoto().getId());
            membro.setFoto(arquivoService.buscaArquivo(membro.getFoto().getId()));
        }
        return daoService.update(membro);
    }

    @Override
    @AcessoMarker(Funcionalidade.CONSULTAR_CONTATOS_IGREJA)
    @AllowAdmin({
            Funcionalidade.MANTER_MEMBROS,
            Funcionalidade.GERENCIAR_ACESSO_MEMBROS
    })
    @AllowMembro({
            Funcionalidade.CONSULTAR_CONTATOS_IGREJA,
            Funcionalidade.REALIZAR_INSCRICAO_EBD
    })
    public Membro buscaMembro(Long membro) {
        return daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), membro));
    }

    @Override
    @AllowAdmin({
            Funcionalidade.MANTER_MEMBROS,
            Funcionalidade.GERENCIAR_ACESSO_MEMBROS
    })
    public Membro aprovaCadastroContato(Long membro) {
        Membro entidade = buscaMembro(membro);

        entidade = trataEmailsReplicados(entidade);

        entidade.ativo();

        return daoService.update(entidade);
    }

    private Membro trataEmailsReplicados(Membro entidade) {
        // Tratamento para e-mails cadastrados de forma replicada
        if (!StringUtil.isEmpty(entidade.getEmail())) {
            List<Membro> todos = daoService.findWith(QueryAdmin.MEMBRO_POR_EMAIL_IGREJA.
                    create(entidade.getEmail().toLowerCase(), entidade.getIgreja().getChave()));

            if (!todos.isEmpty()) {
                if (todos.size() > 1) {
                    Collections.sort(todos, new Comparator<Membro>() {
                        @Override
                        public int compare(Membro o1, Membro o2) {
                            return o1.getId().compareTo(o2.getId());
                        }
                    });

                    for (int i = 1; i < todos.size(); i++) {
                        Membro outro = todos.get(i);
                        outro.exclui();
                        daoService.update(outro);
                    }
                }

                if (entidade.getId() < todos.get(0).getId()) {
                    Membro outro = todos.get(0);
                    outro.exclui();
                    daoService.update(outro);
                } else if (!entidade.getId().equals(todos.get(0).getId())) {
                    entidade.exclui();
                    daoService.update(entidade);
                    entidade = todos.get(0);
                }
            }

        }
        return entidade;
    }

    @Override
    @AllowAdmin({
            Funcionalidade.GERENCIAR_ACESSO_MEMBROS
    })
    public Membro aprovaCadastroMembro(Long membro) {
        Membro entidade = aprovaCadastroContato(membro);

        return darAcessoMembro(entidade.getId());
    }

    @Override
    @AllowAdmin({
            Funcionalidade.MANTER_MEMBROS,
            Funcionalidade.GERENCIAR_ACESSO_MEMBROS
    })
    public void rejeitaCadastro(Long membro) {
        removeMembro(membro);
    }

    @Override
    @AllowAdmin({
            Funcionalidade.MANTER_MEMBROS,
            Funcionalidade.GERENCIAR_ACESSO_MEMBROS
    })
    @AllowMembro({
            Funcionalidade.CONSULTAR_CONTATOS_IGREJA,
            Funcionalidade.REALIZAR_INSCRICAO_EBD
    })
    public BuscaPaginadaDTO<Membro> busca(FiltroMembroDTO filtro) {
        return daoService.findWith(new FiltroMembro(sessaoBean.isAdmin(), sessaoBean.getChaveIgreja(), filtro));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MINISTERIOS)
    public Ministerio cadastra(Ministerio ministerio) {
        ministerio.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        return daoService.create(ministerio);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MINISTERIOS)
    public Ministerio atualiza(Ministerio ministerio) {
        return daoService.update(ministerio);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MINISTERIOS)
    public void removeMinisterio(Long idMinisterio) {
        Ministerio ministerio = buscaMinisterio(idMinisterio);
        ministerio.inativa();
        daoService.update(ministerio);
    }

    @Override
    public Ministerio buscaMinisterio(Long idMinisterio) {
        return daoService.findWith(QueryAdmin.MINISTERIO_ATIVO_POR_IGREJA.
                createSingle(sessaoBean.getChaveIgreja(), idMinisterio));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MINISTERIOS)
    public List<Ministerio> buscaMinisterios() {
        return daoService.findWith(QueryAdmin.MINISTERIOS_ATIVOS.create(sessaoBean.getChaveIgreja()));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public Perfil cadastra(Perfil perfil) {
        perfil.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        return daoService.create(perfil);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public Perfil atualiza(Perfil perfil) {
        return daoService.update(perfil);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public Perfil buscaPerfil(Long perfil) {
        return daoService.find(Perfil.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), perfil));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public void removePerfil(Long perfil) {
        Perfil entidade = buscaPerfil(perfil);
        daoService.delete(Perfil.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
    }

    @Override
    @AllowAdmin({
            Funcionalidade.MANTER_PERFIS,
            Funcionalidade.MANTER_MEMBROS
    })
    public List<Perfil> buscaPerfis() {
        return daoService.findWith(QueryAdmin.PERFIS.create(sessaoBean.getChaveIgreja()));
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_PUBLICACOES, Funcionalidade.MANTER_BOLETINS})
    public Boletim cadastra(Boletim boletim) throws IOException {
        boletim.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        boletim.setBoletim(arquivoService.buscaArquivo(boletim.getBoletim().getId()));

        boolean trataPDF = trataTrocaPDF(boletim);
        if (trataPDF) {
            boletim.processando();
        }

        Boletim entidade = daoService.create(boletim);

        if (trataPDF) {
            batchServie.processaBoletim(boletim.getChaveIgreja(), boletim.getId());
        }

        return entidade;
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_CIFRAS, Funcionalidade.MANTER_CANTICOS})
    public Cifra cadastra(Cifra cifra) throws IOException {
        cifra.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        cifra.setCifra(arquivoService.buscaArquivo(cifra.getCifra().getId()));

        boolean trataPDF = trataTrocaPDF(cifra);
        if (trataPDF) {
            cifra.processando();
        }

        Cifra salvo = daoService.create(cifra);

        if (trataPDF) {
            batchServie.processaCifra(cifra.getChaveIgreja(), cifra.getId());
        }

        return salvo;
    }

    @Override
    @AllowAdmin
    public File buscaAjuda(String path) {
        Igreja igreja = daoService.find(Igreja.class, sessaoBean.getChaveIgreja());
        return new File(new File(new File(ResourceBundleUtil._default().
                getPropriedade("RESOURCES_ROOT"), "ajuda"), igreja.getLocale()), path);
    }

    public AppServiceImpl() {
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_CIFRAS)
    public String extraiTexto(Long idArquivo) {
        try {
            File file = EntityFileManager.get(arquivoService.buscaArquivo(idArquivo), "dados");

            if (!file.exists()) {
                throw new ServiceException("mensagens.MSG-403");
            }

            PDDocument pdffile = PDDocument.load(file);

            StringWriter writer = new StringWriter();
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.writeText(pdffile, writer);

            return writer.toString().trim();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    private boolean trataTrocaPDF(final ArquivoPDF pdf) {
        if (!pdf.getPDF().isUsed()) {
            if (pdf.getId() != null) {
                ArquivoPDF old = daoService.find(pdf.getClass(), new RegistroIgrejaId(sessaoBean.getChaveIgreja(), pdf.getId()));
                List<Arquivo> pages = new ArrayList<Arquivo>();
                if (old != null) {
                    arquivoService.registraDesuso(old.getPDF().getId());
                    if (old.getThumbnail() != null) {
                        arquivoService.registraDesuso(old.getThumbnail().getId());
                    }

                    pages.addAll(old.getPaginas());
                    old.getPaginas().clear();
                    for (Arquivo arq : pages) {
                        arquivoService.registraDesuso(arq.getId());
                    }
                }
            }

            pdf.getPaginas().clear();

            arquivoService.registraUso(pdf.getPDF().getId());

            return true;
        }

        return false;
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_PUBLICACOES, Funcionalidade.MANTER_BOLETINS})
    public Boletim atualiza(Boletim boletim) throws IOException {
        boletim.setBoletim(arquivoService.buscaArquivo(boletim.getBoletim().getId()));
        boletim.setUltimaAlteracao(DateUtil.getDataAtual());
        if (trataTrocaPDF(boletim)) {
            boletim.processando();

            batchServie.processaBoletim(boletim.getChaveIgreja(), boletim.getId());
        }
        return daoService.update(boletim);
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_CIFRAS, Funcionalidade.MANTER_CANTICOS})
    public Cifra atualiza(Cifra cifra) throws IOException {
        cifra.setCifra(arquivoService.buscaArquivo(cifra.getCifra().getId()));
        cifra.setUltimaAlteracao(DateUtil.getDataAtual());

        if (trataTrocaPDF(cifra)) {

            cifra.processando();

            batchServie.processaCifra(cifra.getChaveIgreja(), cifra.getId());
        }

        return daoService.update(cifra);
    }

    @Override
    public Boletim buscaBoletim(Long boletim) {
        Boletim entidade = daoService.find(Boletim.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), boletim));

        if (TipoBoletim.BOLETIM.equals(entidade.getTipo())) {
            AcessoMarkerContext.funcionalidade(Funcionalidade.LISTAR_BOLETINS);
        } else {
            AcessoMarkerContext.funcionalidade(Funcionalidade.LISTAR_PUBLICACOES);
        }

        return entidade;
    }

    @Override
    public Cifra buscaCifra(Long cifra) {
        Cifra entidade = daoService.find(Cifra.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), cifra));

        if (TipoCifra.CIFRA.equals(entidade.getTipo())) {
            AcessoMarkerContext.funcionalidade(Funcionalidade.CONSULTAR_CIFRAS);
        } else {
            AcessoMarkerContext.funcionalidade(Funcionalidade.CONSULTAR_CANTICOS);
        }

        return entidade;
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_PUBLICACOES, Funcionalidade.MANTER_BOLETINS})
    public void removeBoletim(Long boletim) {
        Boletim entidade = buscaBoletim(boletim);

        if (entidade != null) {
            for (Arquivo page : entidade.getPaginas()) {
                arquivoService.registraDesuso(page.getId());
            }

            arquivoService.registraDesuso(entidade.getBoletim().getId());

            if (entidade.getThumbnail() != null) {
                arquivoService.registraDesuso(entidade.getThumbnail().getId());
            }

            daoService.delete(Boletim.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
        }
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_CIFRAS, Funcionalidade.MANTER_CANTICOS})
    public void removeCifra(Long cifra) {
        Cifra entidade = buscaCifra(cifra);

        for (Arquivo page : entidade.getPaginas()) {
            arquivoService.registraDesuso(page.getId());
        }

        arquivoService.registraDesuso(entidade.getCifra().getId());
        arquivoService.registraDesuso(entidade.getThumbnail().getId());
        daoService.delete(Cifra.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_BOLETINS, Funcionalidade.MANTER_PUBLICACOES})
    public BuscaPaginadaDTO<Boletim> buscaTodos(FiltroBoletimDTO filtro) {
        return daoService.findWith(new FiltroBoletim(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<Cifra> busca(FiltroCifraDTO filtro) {
        return daoService.findWith(new FiltroCifra(sessaoBean.isAdmin(), sessaoBean.getChaveIgreja(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<Boletim> buscaPublicados(FiltroBoletimPublicadoDTO filtro) {
        return buscaTodos(filtro);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_DADOS_INSTITUCIONAIS)
    public Institucional atualiza(Institucional institucional) {
        if (institucional.getDivulgacao() != null) {
            institucional.setDivulgacao(arquivoService.buscaArquivo(institucional.getDivulgacao().getId()));
            arquivoService.registraUso(institucional.getDivulgacao().getId());
        }

        return daoService.update(institucional);
    }

    @Override
    public Institucional recuperaInstitucional() {
        Institucional institucional = daoService.find(Institucional.class, sessaoBean.getChaveIgreja());
        if (institucional == null) {
            institucional = new Institucional(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        }
        return institucional;
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public CategoriaEstudo cadastra(CategoriaEstudo categoria) {
        categoria.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        return daoService.create(categoria);
    }

    @Override
    public List<CategoriaEstudo> buscaCategoriasEstudo() {
        if (sessaoBean.isAdmin()) {
            return daoService.findWith(QueryAdmin.CATEGORIA_ESTUDO.create(sessaoBean.getChaveIgreja()));
        } else {
            return daoService.findWith(QueryAdmin.CATEGORIA_USADAS_ESTUDO.create(sessaoBean.getChaveIgreja()));
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public Estudo cadastra(Estudo estudo) {
        estudo.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));

        if (estudo.getCategoria() != null) {
            estudo.setCategoria(daoService.find(CategoriaEstudo.class,
                    new RegistroIgrejaId(sessaoBean.getChaveIgreja(), estudo.getCategoria().getId())));
        }

        estudo.setMembro(buscaMembro(sessaoBean.getIdMembro()));

        boolean trataPDF = trataAtualizacaoPDFEstudo(estudo);

        estudo = daoService.create(estudo);

        scheduleRelatorioEstudo(estudo);

        if (trataPDF) {
            batchServie.processaEstudo(estudo.getChaveIgreja(), estudo.getId());
        }

        return estudo;
    }

    private void scheduleRelatorioEstudo(Estudo estudo) {
        try {
            Template template = daoService.find(Template.class, estudo.getIgreja().getChave());

            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioEstudo(estudo, template), "pdf"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioEstudo(estudo, template), "xls"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioEstudo(estudo, template), "docx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public Estudo atualiza(Estudo estudo) {
        estudo.setCategoria(daoService.find(CategoriaEstudo.class,
                new RegistroIgrejaId(sessaoBean.getChaveIgreja(), estudo.getCategoria().getId())));

        estudo.alterado();

        boolean trataPDF = trataAtualizacaoPDFEstudo(estudo);

        estudo = daoService.update(estudo);

        scheduleRelatorioEstudo(estudo);

        if (trataPDF) {
            batchServie.processaEstudo(estudo.getChaveIgreja(), estudo.getId());
        }

        return estudo;
    }

    private boolean trataAtualizacaoPDFEstudo(Estudo estudo) {
        if (estudo.getPDF() != null) {
            estudo.setPdf(arquivoService.buscaArquivo(estudo.getPDF().getId()));

            if (trataTrocaPDF(estudo)) {
                estudo.processando();

                return true;
            }
        } else {
            estudo.publicado();
        }

        return false;
    }

    @Override
    @AcessoMarker(Funcionalidade.LISTAR_ESTUDOS)
    public Estudo buscaEstudo(Long estudo) {
        return daoService.find(Estudo.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), estudo));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public void removeEstudo(Long estudo) {
        Estudo entidade = buscaEstudo(estudo);
        daoService.delete(Estudo.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public BuscaPaginadaEstudoDTO buscaTodos(FiltroEstudoDTO filtro) {
        BuscaPaginadaDTO<Estudo> resultado = daoService.findWith(new FiltroEstudo(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));

        BuscaPaginadaEstudoDTO estudos = new BuscaPaginadaEstudoDTO(resultado.getResultados(),
                resultado.getTotalResultados(), filtro.getPagina(), filtro.getTotal());

        if (filtro.getCategoria() != null) {
            estudos.setCategoria(daoService.find(CategoriaEstudo.class,
                    new RegistroIgrejaId(sessaoBean.getChaveIgreja(), filtro.getCategoria())));
        }

        return estudos;
    }

    @Override
    public BuscaPaginadaEstudoDTO buscaPublicados(FiltroEstudoPublicadoDTO filtro) {
        return buscaTodos(filtro);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_NOTICIAS)
    public Noticia cadastra(Noticia noticia) {
        noticia.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        noticia.setAutor(buscaMembro(sessaoBean.getIdMembro()));

        if (noticia.getIlustracao() != null) {
            arquivoService.registraUso(noticia.getIlustracao().getId());
            noticia.setIlustracao(arquivoService.buscaArquivo(noticia.getIlustracao().getId()));
        }

        preparaResumo(noticia);
        return daoService.create(noticia);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_NOTICIAS)
    public Noticia atualiza(Noticia noticia) {

        if (noticia.getIlustracao() != null) {
            Arquivo ilustracao = arquivoService.buscaArquivo(noticia.getIlustracao().getId());

            if (!ilustracao.isUsed()) {
                arquivoService.registraUso(noticia.getIlustracao().getId());
            }

            noticia.setIlustracao(ilustracao);
        }

        preparaResumo(noticia);
        return daoService.update(noticia);
    }

    private void preparaResumo(Noticia noticia) {
        String resumo = StringEscapeUtils.unescapeHtml(noticia.getTexto()
                .replaceAll("<[^>]+>", " ").replaceAll("\\s+", " "));

        if (resumo.length() > 500) {
            noticia.setResumo(resumo.substring(0, 497) + "...");
        } else {
            noticia.setResumo(resumo);
        }
    }

    @Override
    public Noticia buscaNoticia(Long noticia) {
        return daoService.find(Noticia.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), noticia));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_NOTICIAS)
    public void removeNoticia(Long noticia) {
        Noticia entidade = buscaNoticia(noticia);
        daoService.delete(Noticia.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_NOTICIAS)
    public BuscaPaginadaDTO<Noticia> buscaTodos(FiltroNoticiaDTO filtro) {
        return daoService.findWith(new FiltroNoticia(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<Noticia> buscaPublicados(FiltroNoticiaPublicadaDTO filtro) {
        return buscaTodos(filtro);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.ENVIAR_NOTIFICACOES)
    public void enviar(Notificacao notificacao) {
        notificacao.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));

        if (StringUtil.isEmpty(notificacao.getTitulo())) {
            notificacao.setTitulo(
                    MessageFormat.format(
                            (String) paramService.get(notificacao.getIgreja().getChave(), TipoParametro.PUSH_TITLE_NOTIFICACAO),
                            notificacao.getIgreja().getNomeAplicativo()
                    )
            );
        }

        notificacao = daoService.create(notificacao);

        FiltroDispositivoNotificacaoDTO filtro = new FiltroDispositivoNotificacaoDTO(notificacao.getIgreja());
        filtro.setApenasMembros(notificacao.isApenasMembros());
        for (Ministerio m : notificacao.getMinisteriosAlvo()) {
            filtro.getMinisterios().add(m.getId());
        }

        enviaPush(filtro, notificacao.getTitulo(), notificacao.getMensagem(), TipoNotificacao.NOTIFICACAO, false);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    @AllowMembro(Funcionalidade.REALIZAR_VOTACAO)
    public ResultadoVotacaoDTO buscaResultado(Long votacao) {
        Votacao entidade = buscaVotacao(votacao);

        if (!entidade.isEncerrado() && !sessaoBean.isAdmin()) {
            throw new ServiceException("mensagens.MSG-053");
        }

        ResultadoVotacaoDTO dto = new ResultadoVotacaoDTO(entidade);
        for (Questao questao : entidade.getQuestoes()) {
            ResultadoQuestaoDTO rq = dto.init(questao);

            for (Opcao o : questao.getOpcoes()) {
                try {
                    rq.resultado(o, ((Number) daoService.findWith(QueryAdmin.RESULTADOS_OPCAO.createSingle(o.getId()))).intValue());
                } catch (NoResultException ex) {
                    rq.resultado(o, 0);
                }
            }

            rq.brancos(nullToZero(daoService.findWith(QueryAdmin.BRANCOS_QUESTAO.createSingle(questao.getId()))).intValue());
            rq.nulos(nullToZero(daoService.findWith(QueryAdmin.NULOS_QUESTAO.createSingle(questao.getId()))).intValue());
        }

        return dto;
    }

    private Number nullToZero(Object val) {
        return val == null ? 0 : (Number) val;
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public Votacao cadastra(Votacao votacao) {
        votacao.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        preencheRelacionamentos(votacao);
        return daoService.update(votacao);
    }

    private void preencheRelacionamentos(Votacao votacao) {
        List<Questao> questoes = new ArrayList<Questao>();
        for (Questao questao : votacao.getQuestoes()) {
            questao.setVotacao(votacao);
            List<Opcao> opcoes = new ArrayList<Opcao>();
            for (Opcao opcao : questao.getOpcoes()) {
                opcao.setQuestao(questao);
                opcoes.add(opcao);
            }
            questao.setOpcoes(opcoes);
            questoes.add(questao);
        }
        votacao.setQuestoes(questoes);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public Votacao atualiza(Votacao votacao) {
        preencheRelacionamentos(votacao);
        return daoService.update(votacao);
    }

    @Override
    @AcessoMarker(Funcionalidade.REALIZAR_VOTACAO)
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    @AllowMembro(Funcionalidade.REALIZAR_VOTACAO)
    public Votacao buscaVotacao(Long votacao) {
        return daoService.find(Votacao.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), votacao));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public void removeVotacao(Long votacao) {
        Votacao entidade = buscaVotacao(votacao);

        if (entidade != null) {
            daoService.execute(QueryAdmin.REMOVER_VOTOS.create(sessaoBean.getChaveIgreja(), votacao));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_OPCAO.create(sessaoBean.getChaveIgreja(), votacao));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_QUESTAO.create(sessaoBean.getChaveIgreja(), votacao));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_VOTACAO.create(sessaoBean.getChaveIgreja(), votacao));
        }

        daoService.delete(Votacao.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public BuscaPaginadaDTO<Votacao> buscaTodas(FiltroVotacaoDTO filtro) {
        BuscaPaginadaDTO<Object[]> busca = daoService.findWith(new FiltroVotacao(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), filtro));

        List<Votacao> votacoes = new ArrayList<>();

        for (Object[] os : busca) {
            Votacao votacao = (Votacao) os[0];
            votacao.setRespondido(((Number) os[1]).intValue() > 0);
            votacoes.add(votacao);
        }

        return new BuscaPaginadaDTO<>(votacoes, busca.getTotalResultados(), busca.getPagina(), filtro.getTotal());
    }

    @Override
    @AllowMembro(Funcionalidade.REALIZAR_VOTACAO)
    public BuscaPaginadaDTO<Votacao> buscaAtivas(FiltroVotacaoAtivaDTO filtro) {
        return buscaTodas(filtro);
    }

    @Audit
    @Override
    @AllowMembro(Funcionalidade.REALIZAR_VOTACAO)
    public void realizarVotacao(RespostaVotacao resposta) {
        daoService.create(resposta);
        daoService.create(new Voto(resposta.getVotacao(), buscaMembro(sessaoBean.getIdMembro())));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)
    public PedidoOracao atende(Long pedidoOracao) {
        PedidoOracao entidade = daoService.find(PedidoOracao.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), pedidoOracao));
        entidade.atende(buscaMembro(sessaoBean.getIdMembro()));
        entidade = daoService.update(entidade);

        enviaPush(new FiltroDispositivoNotificacaoDTO(entidade.getIgreja(),
                        entidade.getSolicitante().getId()),
                (String) paramService.get(entidade.getIgreja().getChave(), TipoParametro.PUSH_TITLE_ATENDIMENTO_PEDIDO_ORACAO),
                MessageFormat.format(
                        (String) paramService.get(entidade.getIgreja().getChave(), TipoParametro.PUSH_BODY_ATENDIMENTO_PEDIDO_ORACAO),
                        mensagemBuilder.formataDataHora(entidade.getDataSolicitacao(), entidade.getIgreja().getLocale(), entidade.getIgreja().getTimezone())),
                TipoNotificacao.PEDIDO_ORACAO, false);

        return entidade;
    }

    @Override
    @AllowAdmin(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)
    public BuscaPaginadaDTO<PedidoOracao> buscaTodos(FiltroPedidoOracaoDTO filtro) {
        return daoService.findWith(new FiltroPedidoOracao(sessaoBean.getIdMembro(), sessaoBean.getChaveIgreja(), filtro));
    }

    @Override
    @AllowMembro(Funcionalidade.PEDIR_ORACAO)
    public BuscaPaginadaDTO<PedidoOracao> buscaMeus(FiltroMeusPedidoOracaoDTO filtro) {
        return buscaTodos(filtro);
    }

    @Audit
    @Override
    @AcessoMarker(Funcionalidade.PEDIR_ORACAO)
    @AllowMembro(Funcionalidade.PEDIR_ORACAO)
    public PedidoOracao realizaPedido(PedidoOracao pedido) {
        pedido.setSolicitante(buscaMembro(sessaoBean.getIdMembro()));
        return daoService.create(pedido);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public AgendamentoAtendimento agenda(Long membro, Long idHorario, Date data) {
        if (!sessaoBean.isAdmin()) {
            return _agenda(buscaMembro(sessaoBean.getIdMembro()), idHorario, data);
        } else {
            return confirma(_agenda(daoService.find(Membro.class,
                    new RegistroIgrejaId(sessaoBean.getChaveIgreja(), membro)), idHorario, data).getId());
        }
    }

    private AgendamentoAtendimento _agenda(Membro membro, Long idHorario, Date data) {
        HorarioAtendimento horario = daoService.find(HorarioAtendimento.class, idHorario);

        if (!sessaoBean.getChaveIgreja().equals(horario.getCalendario().getIgreja().getChave())) {
            throw new ServiceException("mensagens.MSG-604");
        }

        AgendamentoAtendimento atendimento = daoService.create(new AgendamentoAtendimento(membro, horario, data));

        if (!sessaoBean.isAdmin()) {
            enviaPush(new FiltroDispositivoNotificacaoDTO(atendimento.getIgreja(), atendimento.getCalendario().getPastor().getId()),
                    (String) paramService.get(atendimento.getIgreja().getChave(), TipoParametro.PUSH_TITLE_AGENDAMENTO),
                    MessageFormat.format(
                            (String) paramService.get(atendimento.getIgreja().getChave(), TipoParametro.PUSH_BODY_AGENDAMENTO),
                            atendimento.getMembro().getNome(),
                            mensagemBuilder.formataData(atendimento.getDataHoraInicio(),
                                    atendimento.getIgreja().getLocale(),
                                    atendimento.getIgreja().getTimezone()),
                            mensagemBuilder.formataHora(atendimento.getDataHoraInicio(),
                                    atendimento.getIgreja().getLocale(),
                                    atendimento.getIgreja().getTimezone()),
                            mensagemBuilder.formataHora(atendimento.getDataHoraFim(),
                                    atendimento.getIgreja().getLocale(),
                                    atendimento.getIgreja().getTimezone())),
                    TipoNotificacao.ACONSELHAMENTO, false);
        }

        return atendimento;
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public AgendamentoAtendimento confirma(Long id) {
        AgendamentoAtendimento agendamento = buscaAgendamento(id);

        if (!sessaoBean.isAdmin()
                && !agendamento.getCalendario().getPastor().getId().equals(sessaoBean.getIdMembro())) {
            throw new ServiceException("mensagens.MSG-604");
        }

        agendamento.confirmado();
        agendamento = daoService.update(agendamento);

        enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getIgreja(), agendamento.getMembro().getId()),
                (String) paramService.get(agendamento.getIgreja().getChave(), TipoParametro.PUSH_TITLE_CONFIRMACAO_AGENDAMENTO),
                MessageFormat.format(
                        (String) paramService.get(agendamento.getIgreja().getChave(), TipoParametro.PUSH_BODY_CONFIRMACAO_AGENDAMENTO),
                        agendamento.getCalendario().getPastor().getNome(),
                        mensagemBuilder.formataData(agendamento.getDataHoraInicio(),
                                agendamento.getIgreja().getLocale(),
                                agendamento.getIgreja().getTimezone()),
                        mensagemBuilder.formataHora(agendamento.getDataHoraInicio(),
                                agendamento.getIgreja().getLocale(),
                                agendamento.getIgreja().getTimezone()),
                        mensagemBuilder.formataHora(agendamento.getDataHoraFim(),
                                agendamento.getIgreja().getLocale(),
                                agendamento.getIgreja().getTimezone())),
                TipoNotificacao.ACONSELHAMENTO, false);

        return agendamento;
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public AgendamentoAtendimento cancela(Long id) {
        AgendamentoAtendimento agendamento = buscaAgendamento(id);

        if (!sessaoBean.isAdmin()
                && !agendamento.getMembro().getId().equals(sessaoBean.getIdMembro())
                && !agendamento.getCalendario().getPastor().getId().equals(sessaoBean.getIdMembro())) {
            throw new ServiceException("mensagens.MSG-604");
        }

        agendamento.cancelado();

        agendamento = daoService.update(agendamento);

        if (sessaoBean.isAdmin() ||
                agendamento.getCalendario().getPastor().getId().equals(sessaoBean.getIdMembro())) {
            enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getIgreja(),
                            agendamento.getMembro().getId()),
                    (String) paramService.get(agendamento.getIgreja().getChave(), TipoParametro.PUSH_TITLE_CANCELAMENTO_AGENDAMENTO),
                    MessageFormat.format(
                            (String) paramService.get(agendamento.getIgreja().getChave(), TipoParametro.PUSH_BODY_CANCELAMENTO_AGENDAMENTO),
                            agendamento.getCalendario().getPastor().getNome(),
                            mensagemBuilder.formataData(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            mensagemBuilder.formataHora(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            mensagemBuilder.formataHora(agendamento.getDataHoraFim(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone())),
                    TipoNotificacao.ACONSELHAMENTO, false);
        } else {
            enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getIgreja(),
                            agendamento.getCalendario().getPastor().getId()),
                    (String) paramService.get(agendamento.getIgreja().getChave(), TipoParametro.PUSH_TITLE_CANCELAMENTO_AGENDAMENTO_MEMBRO),
                    MessageFormat.format(
                            (String) paramService.get(agendamento.getIgreja().getChave(), TipoParametro.PUSH_BODY_CANCELAMENTO_AGENDAMENTO_MEMBRO),
                            agendamento.getMembro().getNome(),
                            mensagemBuilder.formataData(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            mensagemBuilder.formataHora(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            mensagemBuilder.formataHora(agendamento.getDataHoraFim(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone())),
                    TipoNotificacao.ACONSELHAMENTO, false);
        }

        return agendamento;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public AgendamentoAtendimento buscaAgendamento(Long agendamento) {
        return daoService.find(AgendamentoAtendimento.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), agendamento));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public List<AgendamentoAtendimento> buscaAgendamentos(CalendarioAtendimento calendario, Date dataInicio, Date dataTermino) {
        Date dataAtual = DateUtil.getDataAtual();
        return daoService.findWith(QueryAdmin.AGENDAMENTOS_ATENDIMENTO.
                create(sessaoBean.getChaveIgreja(), calendario.getId(),
                        dataInicio.before(dataAtual) ? dataAtual : dataInicio, dataTermino));
    }

    @Override
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public BuscaPaginadaDTO<AgendamentoAtendimento> buscaMeusAgendamentos(FiltroMeusAgendamentoDTO filtro) {
        return daoService.findWith(new FiltroMeusAgendamentos(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), filtro));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public CalendarioAtendimento cadastra(CalendarioAtendimento calendario) {
        calendario.setPastor(buscaMembro(calendario.getPastor().getId()));
        calendario.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        return daoService.create(calendario);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public CalendarioAtendimento buscaCalendario(Long calendario) {
        return daoService.find(CalendarioAtendimento.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), calendario));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public void removeCalendario(Long calendario) {
        CalendarioAtendimento entidade = buscaCalendario(calendario);
        entidade.inativa();
        daoService.update(entidade);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public List<CalendarioAtendimento> buscaCalendarios() {
        return daoService.findWith(QueryAdmin.CALENDARIOS.create(sessaoBean.getChaveIgreja()));
    }

    private HorarioAtendimento buscaHorario(Long calendario, Long horario) {
        HorarioAtendimento h = daoService.find(HorarioAtendimento.class, horario);

        if (!h.getCalendario().getId().equals(calendario)) {
            throw new ServiceException("mensagens.MSG-604");
        }

        return h;
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public void cadastra(Long idCalendario, HorarioAtendimento horario) {
        CalendarioAtendimento calendario = buscaCalendario(idCalendario);
        horario.setCalendario(calendario);

        for (DiaSemana ds : horario.getDiasSemana()) {
            HorarioAtendimento copia = horario.copy();
            copia.setDiasSemana(Arrays.asList(ds));
            daoService.create(copia);
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public void removeDia(Long calendario, Long idHorario, Date data) {
        removePeriodo(calendario, idHorario, data, data);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public void removePeriodo(Long calendario, Long idHorario, Date inicio, Date termino) {
        HorarioAtendimento horario = buscaHorario(calendario, idHorario);

        if (termino != null) {
            HorarioAtendimento futuro = horario.copy();
            futuro.setDataInicio(DateUtil.incrementaDias(termino, 1));
            if ((futuro.getDataFim() == null || DateUtil.
                    compareSemHoras(futuro.getDataInicio(), futuro.getDataFim()) <= 0)
                    && !futuro.getDiasSemana().isEmpty()) {
                daoService.create(futuro);
            }
        } else {
            HorarioAtendimento futuro = horario.copy();
            futuro.setDataInicio(DateUtil.incrementaDias(inicio, 1));
            futuro.removeDiaSemana(DiaSemana.get(DateUtil.criarCalendario(inicio).get(Calendar.DAY_OF_WEEK)));
            if ((futuro.getDataFim() == null || DateUtil.
                    compareSemHoras(futuro.getDataInicio(), futuro.getDataFim()) <= 0)
                    && !futuro.getDiasSemana().isEmpty()) {
                daoService.create(futuro);
            }
        }

        horario.setDataFim(DateUtil.decrementaDia(inicio, 1));
        if ((horario.getDataInicio() == null || DateUtil.
                compareSemHoras(horario.getDataInicio(), horario.getDataFim()) <= 0)
                && !horario.getDiasSemana().isEmpty()) {
            daoService.update(horario);
        } else {
            daoService.delete(HorarioAtendimento.class, horario.getId());
        }
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public List<EventoAgendaDTO> buscaAgenda(Long idCalendario, Date dataInicio, Date dataTermino) {
        Date dataAtual = DateUtil.incrementaHoras(DateUtil.getDataAtual(), 3);

        dataInicio = dataAtual.before(dataInicio) ? dataInicio : dataAtual;

        CalendarioAtendimento calendario = buscaCalendario(idCalendario);

        List<EventoAgendaDTO> eventos = new ArrayList<EventoAgendaDTO>();
        List<HorarioAtendimento> horarios = daoService.
                findWith(QueryAdmin.HORARIOS_POR_PERIODO.
                        create(idCalendario, dataInicio, dataTermino));

        TimeZone timeZone = TimeZone.getTimeZone(calendario.getIgreja().getTimezone());
        Calendar cal = Calendar.getInstance(timeZone);
        for (Date data = dataInicio;
             DateUtil.compareSemHoras(data, dataTermino) <= 0;
             data = DateUtil.incrementaDias(data, 1)) {
            cal.setTime(data);
            for (HorarioAtendimento horario : horarios) {
                if (horario.contains(cal)) {
                    Date dti = horario.getInicio(timeZone, data);
                    Date dtf = horario.getFim(timeZone, data);
                    if (dtf.after(dataInicio) && dti.before(dataTermino)
                            && !temAgendamento(calendario, dti, dtf)) {
                        eventos.add(new EventoAgendaDTO(dti, dtf, horario));
                    }
                }
            }
        }
        return eventos;
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public Evento cadastra(Evento evento) {
        evento.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));

        if (evento.getBanner() != null) {
            arquivoService.registraUso(evento.getBanner().getId());
            evento.setBanner(arquivoService.buscaArquivo(evento.getBanner().getId()));
        }

        evento = daoService.create(evento);
        scheduleRelatoriosInscritos(evento);
        return evento;
    }

    private void scheduleRelatoriosInscritos(Evento evento) {
        try {
            Template template = daoService.find(Template.class, evento.getIgreja().getChave());

            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioInscritos(evento, template), "pdf"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioInscritos(evento, template), "xls"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioInscritos(evento, template), "docx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public Evento atualiza(Evento evento) {
        evento.alterado();

        if (evento.getBanner() != null) {
            arquivoService.registraUso(evento.getBanner().getId());
            evento.setBanner(arquivoService.buscaArquivo(evento.getBanner().getId()));
        }

        evento = daoService.update(evento);
        scheduleRelatoriosInscritos(evento);
        return evento;
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public Evento buscaEvento(Long evento) {
        Evento entidade = daoService.find(Evento.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), evento));
        entidade.setVagasRestantes(entidade.getLimiteInscricoes() - ((Number) daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento))).intValue());

        if (TipoEvento.EVENTO.equals(entidade.getTipo())) {
            AcessoMarkerContext.funcionalidade(Funcionalidade.REALIZAR_INSCRICAO_EVENTO);
        } else {
            AcessoMarkerContext.funcionalidade(Funcionalidade.REALIZAR_INSCRICAO_EBD);
        }

        return entidade;
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public void removeEvento(Long evento) {
        Evento entidade = buscaEvento(evento);

        entidade.inativo();

        daoService.update(entidade);
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public BuscaPaginadaDTO<Evento> buscaTodos(FiltroEventoDTO filtro) {
        BuscaPaginadaDTO<Object[]> dados = daoService.findWith(new FiltroEvento(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));

        List<Evento> eventos = new ArrayList<>();
        for (Object[] tupla : dados) {
            Evento evento = new Evento();
            evento.setId(((Number) tupla[0]).longValue());
            evento.setNome((String) tupla[1]);
            evento.setDataHoraInicio((Date) tupla[2]);
            evento.setDataHoraTermino((Date) tupla[3]);
            evento.setDataInicioInscricao((Date) tupla[4]);
            evento.setDataTerminoInscricao((Date) tupla[5]);
            evento.setLimiteInscricoes(((Number) tupla[6]).intValue());
            evento.setVagasRestantes(evento.getLimiteInscricoes() - ((Number) tupla[7]).intValue());
            eventos.add(evento);
        }

        return new BuscaPaginadaDTO<>(eventos, dados.getTotalResultados(), dados.getPagina(), filtro.getTotal());
    }

    @Override
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public BuscaPaginadaDTO<Evento> buscaFuturos(FiltroEventoFuturoDTO filtro) {
        return buscaTodos(filtro);
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public BuscaPaginadaDTO<InscricaoEvento> buscaTodas(Long evento, FiltroInscricaoDTO filtro) {
        return daoService.findWith(new FiltroInscricao(evento, sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), filtro));
    }

    @Override
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public BuscaPaginadaDTO<InscricaoEvento> buscaMinhas(Long evento, FiltroMinhasInscricoesDTO filtro) {
        return buscaTodas(evento, filtro);
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public void confirmaInscricao(Long evento, Long inscricao) {
        InscricaoEvento entidade = daoService.find(InscricaoEvento.class,
                new InscricaoEventoId(inscricao, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), evento)));

        entidade.confirmada();

        daoService.update(entidade);

        scheduleRelatoriosInscritos(entidade.getEvento());
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public void cancelaInscricao(Long evento, Long inscricao) {
        InscricaoEvento entidade = daoService.find(InscricaoEvento.class,
                new InscricaoEventoId(inscricao, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), evento)));

        entidade.cancelada();

        daoService.update(entidade);

        scheduleRelatoriosInscritos(entidade.getEvento());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario cadastra(VersiculoDiario versiculoDiario) {
        versiculoDiario.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        Number minimo = daoService.findWith(QueryAdmin.MENOR_ENVIO_VERSICULOS.createSingle(sessaoBean.getChaveIgreja()));
        if (minimo != null) {
            versiculoDiario.setEnvios(minimo.intValue());
        }
        return daoService.create(versiculoDiario);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario desabilita(Long versiculo) {
        VersiculoDiario entidade = buscaVersiculo(versiculo);
        entidade.desabilitado();
        return daoService.update(entidade);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario habilita(Long versiculo) {
        VersiculoDiario entidade = buscaVersiculo(versiculo);
        entidade.habilitado();
        Number minimo = daoService.findWith(QueryAdmin.MENOR_ENVIO_VERSICULOS.createSingle(sessaoBean.getChaveIgreja()));
        if (minimo != null) {
            entidade.setEnvios(minimo.intValue());
        }
        return daoService.update(entidade);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario buscaVersiculo(Long versiculoDiario) {
        return daoService.find(VersiculoDiario.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), versiculoDiario));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public void removeVersiculo(Long versiculoDiario) {
        VersiculoDiario entidade = buscaVersiculo(versiculoDiario);
        daoService.delete(VersiculoDiario.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public BuscaPaginadaDTO<VersiculoDiario> busca(FiltroVersiculoDiarioDTO filtro) {
        return daoService.findWith(new FiltroVersiculoDiario(sessaoBean.getChaveIgreja(), filtro));
    }

    private boolean temAgendamento(CalendarioAtendimento calendario, Date dti, Date dtf) {
        return daoService.findWith(QueryAdmin.AGENDAMENTO_EM_CHOQUE.createSingle(calendario.getId(), dti, dtf)) != null;
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public ResultadoInscricaoDTO realizaInscricao(List<InscricaoEvento> inscricoes) {
        if (!inscricoes.isEmpty()) {
            Evento evento = inscricoes.get(0).getEvento();
            Membro membro = buscaMembro(sessaoBean.getIdMembro());

            Number qtde = daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento.getId()));
            if (qtde.intValue() + inscricoes.size() > evento.getLimiteInscricoes()) {
                throw new ServiceException("mensagens.MSG-034");
            }

            List<InscricaoEvento> cadastradas = new ArrayList<InscricaoEvento>();
            for (InscricaoEvento inscricao : inscricoes) {

                if (evento.isEBD()) {
                    List<InscricaoEvento> outrasInscricoes = daoService.findWith(QueryAdmin.BUSCA_NSCRICOES_EMAIL
                            .create(TipoEvento.EBD, evento.getChaveIgreja(), inscricao.getEmailInscrito().toLowerCase()));

                    if (!outrasInscricoes.isEmpty() && !outrasInscricoes.contains(inscricao)) {
                        throw new ServiceException("O e-mail " + inscricao.getEmailInscrito() + " já está matriculado em " +
                                outrasInscricoes.get(0).getEvento().getNome() + ". Por favor, procure nossa equipe no estande EBD, aos domingos.");
                    }
                }

                inscricao.setMembro(membro);
                cadastradas.add(daoService.create(inscricao));
            }

            if (sessaoBean.isAdmin()) {
                for (InscricaoEvento inscricao : cadastradas) {
                    inscricao.confirmada();
                    daoService.update(inscricao);
                }
            } else if (evento.isComPagamento()) {
                BigDecimal valorTotal = BigDecimal.ZERO;

                for (InscricaoEvento inscricao : cadastradas) {
                    valorTotal = valorTotal.add(inscricao.getValor());
                }

                ConfiguracaoIgrejaDTO configuracao = buscaConfiguracao();
                if (configuracao != null && configuracao.isHabilitadoPagSeguro()) {
                    String referencia = sessaoBean.getChaveIgreja().toUpperCase() +
                            Long.toString(System.currentTimeMillis(), 36).toUpperCase();

                    PagSeguroService.Pedido pedido = new PagSeguroService.Pedido(referencia,
                            new PagSeguroService.Solicitante(membro.getNome(), membro.getEmail()));

                    for (InscricaoEvento inscricao : inscricoes) {
                        pedido.add(new PagSeguroService.ItemPedido(
                                Long.toString(inscricao.getId(), 36).toUpperCase(),
                                MessageFormat.format(
                                        (String) paramService.get(evento.getIgreja().getChave(), TipoParametro.ITEM_INSCRICAO_PAGSEGURO),
                                        inscricao.getEvento().getNome(), inscricao.getNomeInscrito()),
                                1,
                                inscricao.getValor()
                        ));
                    }

                    String checkout = pagSeguroService.realizaCheckout(pedido, configuracao);

                    for (InscricaoEvento inscricao : cadastradas) {
                        inscricao.setChaveCheckout(checkout);
                        inscricao.setReferenciaCheckout(referencia);
                        daoService.update(inscricao);
                    }

                    Locale locale = Locale.forLanguageTag(evento.getIgreja().getLocale());
                    NumberFormat nformat = NumberFormat.getCurrencyInstance(locale);

                    notificacaoService.sendNow(
                            mensagemBuilder.email(
                                    evento.getIgreja(),
                                    TipoParametro.EMAIL_SUBJECT_PAGAMENTO_INSCRICAO,
                                    TipoParametro.EMAIL_BODY_PAGAMENTO_INSCRICAO,
                                    membro.getNome(),
                                    evento.getNome(),
                                    nformat.format(pedido.getTotal()),
                                    checkout
                            ),
                            new FiltroEmailDTO(evento.getIgreja(), membro.getId()));

                    return new ResultadoInscricaoDTO(checkout);
                }
            }

            scheduleRelatoriosInscritos(evento);
        }

        return new ResultadoInscricaoDTO();
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public ConfiguracaoIgrejaDTO atualiza(ConfiguracaoIgrejaDTO configuracao) {
        ValidationException validation = ValidationException.build();

        if (!StringUtil.isEmpty(configuracao.getTituloAniversario()) &&
                configuracao.getTituloAniversario().length() > 30) {
            validation.add("tituloAniversario", "mensagens.MSG-010");
        }

        if (!StringUtil.isEmpty(configuracao.getTituloBoletim()) &&
                configuracao.getTituloBoletim().length() > 30) {
            validation.add("tituloBoletim", "mensagens.MSG-010");
        }

        if (!StringUtil.isEmpty(configuracao.getTituloVersiculoDiario()) &&
                configuracao.getTituloVersiculoDiario().length() > 30) {
            validation.add("tituloVersiculoDiario", "mensagens.MSG-010");
        }

        if (!StringUtil.isEmpty(configuracao.getTextoAniversario()) &&
                configuracao.getTextoAniversario().length() > 250) {
            validation.add("textoAniversario", "mensagens.MSG-010");
        }

        if (!StringUtil.isEmpty(configuracao.getTextoBoletim()) &&
                configuracao.getTextoBoletim().length() > 250) {
            validation.add("textoBoletim", "mensagens.MSG-010");
        }

        validation.validate();

        paramService.salvaConfiguracao(configuracao, sessaoBean.getChaveIgreja());
        return buscaConfiguracao();
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public ConfiguracaoIgrejaDTO buscaConfiguracao() {
        return paramService.buscaConfiguracao(sessaoBean.getChaveIgreja());
    }

    @Override
    public void verificaPagSeguroPorCodigo(String code) {
        ConfiguracaoIgrejaDTO configuracao = paramService.buscaConfiguracao(sessaoBean.getChaveIgreja());
        atualizaSituacaoPagSeguro(pagSeguroService.buscaReferenciaPorCodigo(code, configuracao), configuracao);
    }

    @Override
    public void verificaPagSeguroPorIdTransacao(String transactionId) {
        ConfiguracaoIgrejaDTO configuracao = paramService.buscaConfiguracao(sessaoBean.getChaveIgreja());
        atualizaSituacaoPagSeguro(pagSeguroService.buscaReferenciaIdTransacao(transactionId, configuracao), configuracao);
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public String buscaURLAutenticacaoCalendar() throws IOException {
        return googleService.getURLAutorizacaoCalendar(sessaoBean.getChaveIgreja(),
                ResourceBundleUtil._default().getPropriedade("OAUTH_CALENDAR_REDIRECT_URL"));
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public String buscaURLAutenticacaoYouTube() throws IOException {
        return googleService.getURLAutorizacaoYouTube(sessaoBean.getChaveIgreja(),
                ResourceBundleUtil._default().getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"));
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_VIDEOS_FACEBOOK)
    public String buscaURLAutenticacaoVideosFacebook() throws IOException {
        return facebookService.getLoginUrlVideos(sessaoBean.getChaveIgreja());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public ConfiguracaoCalendarIgrejaDTO atualiza(ConfiguracaoCalendarIgrejaDTO configuracao) {
        paramService.salvaConfiguracaoCalendar(configuracao, sessaoBean.getChaveIgreja());
        return paramService.buscaConfiguracaoCalendar(sessaoBean.getChaveIgreja());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public List<CalendarioGoogleDTO> buscaVisoesCalendar() throws IOException {
        return googleService.buscaCalendarios(sessaoBean.getChaveIgreja());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_VIDEOS_FACEBOOK)
    public List<PaginaFacebookDTO> buscaPaginasFacebook() throws IOException {
        return facebookService.buscaPaginas(sessaoBean.getChaveIgreja());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public ConfiguracaoYouTubeIgrejaDTO atualiza(ConfiguracaoYouTubeIgrejaDTO configuracao) {
        paramService.salvaConfiguracaoYouTube(configuracao, sessaoBean.getChaveIgreja());
        return paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveIgreja());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_VIDEOS_FACEBOOK)
    public ConfiguracaoFacebookIgrejaDTO atualiza(ConfiguracaoFacebookIgrejaDTO configuracao) {
        paramService.salvaConfiguracaoVideosFacebook(configuracao, sessaoBean.getChaveIgreja());
        return paramService.buscaConfiguracaoVideosFacebook(sessaoBean.getChaveIgreja());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public void iniciaConfiguracaoCalendar(String code) {
        try {
            googleService.saveCredentialsGoogleCalendar(sessaoBean.getChaveIgreja(),
                    ResourceBundleUtil._default().getPropriedade("OAUTH_CALENDAR_REDIRECT_URL"), code);

            ConfiguracaoCalendarIgrejaDTO config = paramService.buscaConfiguracaoCalendar(sessaoBean.getChaveIgreja());
            config.setIdCalendario(googleService.buscaIdsCalendar(sessaoBean.getChaveIgreja()));
            paramService.salvaConfiguracaoCalendar(config, sessaoBean.getChaveIgreja());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new ServiceException("mensagens.MSG-049");
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_VIDEOS_FACEBOOK)
    public void iniciaConfiguracaoVideosFacebook(String code) {
        facebookService.login(sessaoBean.getChaveIgreja(), code);

        ConfiguracaoFacebookIgrejaDTO config = paramService.buscaConfiguracaoVideosFacebook(sessaoBean.getChaveIgreja());

        List<PaginaFacebookDTO> paginas = facebookService.buscaPaginas(sessaoBean.getChaveIgreja());

        if (!paginas.isEmpty()) {
            config.setPagina(paginas.get(0).getId());
        }

        paramService.salvaConfiguracaoVideosFacebook(config, sessaoBean.getChaveIgreja());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public void iniciaConfiguracaoYouTube(String code) {
        try {
            googleService.saveCredentialsYouTube(sessaoBean.getChaveIgreja(),
                    ResourceBundleUtil._default().getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), code);

            ConfiguracaoYouTubeIgrejaDTO config = paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveIgreja());
            config.setIdCanal(googleService.buscaIdCanalYouTube(sessaoBean.getChaveIgreja()));
            paramService.salvaConfiguracaoYouTube(config, sessaoBean.getChaveIgreja());

            Institucional institucional = recuperaInstitucional();
            if (!institucional.getRedesSociais().containsKey("youtube")) {
                institucional.getRedesSociais().put("youtube", "https://www.youtube.com/channel/" + config.getIdCanal());
                daoService.update(institucional);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new ServiceException("mensagens.MSG-046");
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public void desvinculaCalendar() {
        paramService.set(sessaoBean.getChaveIgreja(),
                TipoParametro.GOOGLE_CALENDAR_ID, null);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public void desvinculaYouTube() {
        paramService.set(sessaoBean.getChaveIgreja(),
                TipoParametro.YOUTUBE_CHANNEL_ID, null);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_VIDEOS_FACEBOOK)
    public void desvinculaVideosFacebook() {
        paramService.set(sessaoBean.getChaveIgreja(),
                TipoParametro.FACEBOOK_APP_CODE, null);
    }

    @Override
    @AcessoMarker(Funcionalidade.AGENDA)
    public BuscaPaginadaEventosCalendarioDTO buscaEventos(Integer pagina, Integer total) {
        BuscaPaginadaDTO<EventoCalendario> eventos = daoService.findWith(QueryAdmin.EVENTOS_CALENDARIO_IGREJA.createPaginada(
                pagina, sessaoBean.getChaveIgreja(), total
        ));

        List<EventoCalendarioDTO> eventosDTO = new ArrayList<>();

        for (EventoCalendario e : eventos) {
            eventosDTO.add(new EventoCalendarioDTO(
                    e.getId(),
                    e.getInicio(),
                    e.getTermino(),
                    e.getDescricao(),
                    e.getLocal()
            ));
        }

        return new BuscaPaginadaEventosCalendarioDTO(eventosDTO,
                eventos.isHasProxima() ? Integer.toString(pagina + 1) : null);
    }

    @Override
    @AcessoMarker(Funcionalidade.YOUTUBE)
    public List<VideoDTO> buscaVideosYouTube() {
        try {
            return googleService.buscaVideosYouTube(sessaoBean.getChaveIgreja());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<VideoDTO> buscaVideosFacebook() {
        try {
            return facebookService.buscaVideos(sessaoBean.getChaveIgreja());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public ConfiguracaoCalendarIgrejaDTO buscaConfiguracaoCalendar() {
        return paramService.buscaConfiguracaoCalendar(sessaoBean.getChaveIgreja());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public ConfiguracaoYouTubeIgrejaDTO buscaConfiguracaoYouTube() {
        return paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveIgreja());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_VIDEOS_FACEBOOK)
    public ConfiguracaoFacebookIgrejaDTO buscaConfiguracaoVideosFacebook() {
        return paramService.buscaConfiguracaoVideosFacebook(sessaoBean.getChaveIgreja());
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_PLANOS_LEITURA_BIBLICA)
    @AllowMembro(Funcionalidade.CONSULTAR_PLANOS_LEITURA_BIBLICA)
    public BuscaPaginadaDTO<PlanoLeituraBiblica> buscaTodos(FiltroPlanoLeituraBiblicaDTO filtro) {
        return daoService.findWith(new FiltroPlanoLeituraBiblica(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_PLANOS_LEITURA_BIBLICA)
    @AllowMembro(Funcionalidade.CONSULTAR_PLANOS_LEITURA_BIBLICA)
    public PlanoLeituraBiblica buscaPlanoLeitura(Long idPlano) {
        return daoService.find(PlanoLeituraBiblica.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), idPlano));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PLANOS_LEITURA_BIBLICA)
    public PlanoLeituraBiblica cadastra(PlanoLeituraBiblica plano) {
        plano.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        preencheRelacionamentos(plano);
        return daoService.create(plano);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PLANOS_LEITURA_BIBLICA)
    public PlanoLeituraBiblica atualiza(PlanoLeituraBiblica plano) {
        preencheRelacionamentos(plano);
        plano.alterado();
        return daoService.update(plano);
    }

    private void preencheRelacionamentos(PlanoLeituraBiblica plano) {
        List<DiaLeituraBiblica> dias = new ArrayList<DiaLeituraBiblica>();
        for (DiaLeituraBiblica dia : plano.getDias()) {
            dia.setPlano(plano);
            dias.add(dia);
        }
        plano.setDias(dias);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PLANOS_LEITURA_BIBLICA)
    public void removePlanoLeitura(Long idPlano) {
        PlanoLeituraBiblica plano = buscaPlanoLeitura(idPlano);

        if (plano != null) {
            daoService.execute(QueryAdmin.REMOVE_OPCAO_PLANO.create(sessaoBean.getChaveIgreja(), idPlano));
            daoService.execute(QueryAdmin.REMOVE_MARCACAO_PLANO.create(sessaoBean.getChaveIgreja(), idPlano));

            daoService.delete(PlanoLeituraBiblica.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), idPlano));
        }

    }

    @Override
    public BuscaPaginadaDTO<LeituraBibliaDTO> selecionaPlano(Long plano) {
        desselecionaPlano();

        daoService.create(new OpcaoLeituraBiblica(
                daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro())),
                daoService.find(PlanoLeituraBiblica.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), plano))
        ));

        return buscaPlanoSelecionado(null, 1, 10);
    }

    @Override
    @AllowMembro(Funcionalidade.CONSULTAR_PLANOS_LEITURA_BIBLICA)
    public BuscaPaginadaDTO<LeituraBibliaDTO> buscaPlanoSelecionado(Date ultimaAlteracao, int pagina, int total) {
        if (ultimaAlteracao == null) {
            ultimaAlteracao = DateUtil.getDataZero();
        } else if (ultimaAlteracao.getTime() > System.currentTimeMillis()) {
            ultimaAlteracao = new Date();
        }

        BuscaPaginadaDTO<LeituraBibliaDTO> busca = daoService.findWith(QueryAdmin.
                LEITURA_SELECIONADA.createPaginada(pagina, sessaoBean.getChaveIgreja(),
                sessaoBean.getIdMembro(), new Date(ultimaAlteracao.getTime() + 1), total));

        for (LeituraBibliaDTO leitura : busca.getResultados()) {
            leitura.setLido((MarcacaoLeituraBiblica) daoService.findWith(QueryAdmin.MARCACAO_LEITURA_DIA.
                    createSingle(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), leitura.getDia().getId())));
        }

        return busca;
    }

    @Override
    @AllowMembro(Funcionalidade.CONSULTAR_PLANOS_LEITURA_BIBLICA)
    public PlanoLeituraBiblica buscaPlanoSelecionado() {
        OpcaoLeituraBiblica opcao = daoService.findWith(QueryAdmin.OPCAO_PLANO_LEITURA_SELECIONADA.createSingle(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));

        if (opcao != null) {
            return opcao.getPlanoLeitura();
        }

        return null;
    }

    @Override
    @AllowMembro(Funcionalidade.CONSULTAR_PLANOS_LEITURA_BIBLICA)
    public void desselecionaPlano() {
        OpcaoLeituraBiblica opcao = daoService.findWith(QueryAdmin.OPCAO_PLANO_LEITURA_SELECIONADA.createSingle(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));

        if (opcao != null) {
            opcao.encerra();
            daoService.update(opcao);
        }
    }

    @Override
    @AllowMembro(Funcionalidade.CONSULTAR_PLANOS_LEITURA_BIBLICA)
    public LeituraBibliaDTO desmarcaLeitura(Long dia) {
        MarcacaoLeituraBiblica marcacao = daoService.findWith(QueryAdmin.MARCACAO_LEITURA_DIA.
                createSingle(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), dia));

        if (marcacao != null) {
            daoService.delete(MarcacaoLeituraBiblica.class, marcacao.getId());

            return new LeituraBibliaDTO(marcacao.getDia(), false);
        }

        return null;
    }

    @Override
    @AllowMembro(Funcionalidade.CONSULTAR_PLANOS_LEITURA_BIBLICA)
    public LeituraBibliaDTO marcaLeitura(Long dia) {
        if (dia == null) return null;

        MarcacaoLeituraBiblica marcacao = daoService.findWith(QueryAdmin.MARCACAO_LEITURA_DIA.
                createSingle(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), dia));

        if (marcacao == null) {
            DiaLeituraBiblica diaLeitura = daoService.find(DiaLeituraBiblica.class, dia);

            if (diaLeitura != null) {
                marcacao = daoService.create(new MarcacaoLeituraBiblica(
                        daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro())),
                        diaLeitura));

                return new LeituraBibliaDTO(marcacao.getDia(), true);
            }
        }

        return null;
    }

    @Override
    @AllowMembro(Funcionalidade.ANIVERSARIANTES)
    public List<Membro> buscaProximosAniversariantes() {
        Igreja igreja = daoService.find(Igreja.class, sessaoBean.getChaveIgreja());

        TimeZone timeZone = TimeZone.getTimeZone(igreja.getTimezone());
        Calendar dateCal = Calendar.getInstance(timeZone);
        dateCal.setTime(new Date());

        int inicio = (dateCal.get(Calendar.MONTH) + 1) * 100 + dateCal.get(Calendar.DAY_OF_MONTH);

        dateCal.add(Calendar.DAY_OF_MONTH, 30);

        int fim = (dateCal.get(Calendar.MONTH) + 1) * 100 + dateCal.get(Calendar.DAY_OF_MONTH);

        if (inicio < fim) {
            return daoService.findWith(QueryAdmin.PROXIMOS_ANIVERSARIANTES.create(igreja.getChave(), inicio, fim));
        } else {
            List<Membro> aniversariantes = new ArrayList<>();
            aniversariantes.addAll(daoService.findWith(QueryAdmin
                    .PROXIMOS_ANIVERSARIANTES.create(igreja.getChave(), inicio, 1231)));
            aniversariantes.addAll(daoService.findWith(QueryAdmin
                    .PROXIMOS_ANIVERSARIANTES.create(igreja.getChave(), 101, fim)));
            return aniversariantes;
        }

    }

    @Override
    @AllowAdmin
    public List<QuantidadeDispositivoDTO> buscaQuantidadesDispositivos() {
        return daoService.findWith(QueryAdmin.QUANTIDADE_DISPOSITIVOS_BY_IGREJA.create(sessaoBean.getChaveIgreja()));
    }

    @Override
    @AllowAdmin
    public List<EstatisticaDispositivo> buscaEstatisticasDispositivos() {
        return daoService.findWith(QueryAdmin.ESTATISTICAS_DISPOSITIVOS_BY_IGREJA.create(sessaoBean.getChaveIgreja()));
    }

    @Override
    @AllowAdmin
    public List<EstatisticaAcesso> buscaEstatisticasAcessoFuncionalidade(Funcionalidade funcionalidade) {
        return daoService.findWith(QueryAdmin.ESTATISTICAS_ACESSO_BY_IGREJA_AND_FUNCIONALIDADE
                .create(sessaoBean.getChaveIgreja(), funcionalidade.getCodigo()));
    }

    @Override
    public List<CategoriaAudio> buscaCategoriasAudio() {
        if (sessaoBean.isAdmin()) {
            return daoService.findWith(QueryAdmin.CATEGORIA_AUDIO.create(sessaoBean.getChaveIgreja()));
        } else {
            return daoService.findWith(QueryAdmin.CATEGORIA_USADAS_AUDIO.create(sessaoBean.getChaveIgreja()));
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public CategoriaAudio cadastra(CategoriaAudio categoria) {
        categoria.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        return daoService.create(categoria);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public Audio cadastra(Audio audio) {
        audio.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));

        audio.setTipo(TipoAudio.LOCAL);

        audio.setCategoria(daoService.find(CategoriaAudio.class, new RegistroIgrejaId(
                sessaoBean.getChaveIgreja(), audio.getCategoria().getId()
        )));

        audio.setAudio(arquivoService.buscaArquivo(audio.getAudio().getId()));

        arquivoService.registraUso(audio.getAudio().getId());

        if (audio.getCapa() != null) {
            audio.setCapa(arquivoService.buscaArquivo(audio.getCapa().getId()));

            arquivoService.registraUso(audio.getCapa().getId());
        }

        File file = EntityFileManager.get(audio.getAudio(), "dados");

        audio.setTamamnhoArquivo(file.length());

        try {
            Mp3File mp3 = new Mp3File(file);
            audio.setTempoAudio(mp3.getLengthInSeconds());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return daoService.create(audio);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public Audio atualiza(Audio audio) {
        if (!TipoAudio.LOCAL.equals(audio.getTipo())) {
            throw new ServiceException("mensagens.MSG-403");
        }

        audio.setCategoria(daoService.find(CategoriaAudio.class, new RegistroIgrejaId(
                sessaoBean.getChaveIgreja(), audio.getCategoria().getId()
        )));

        Audio entidade = daoService.find(Audio.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), audio.getId()));

        if (audio.getCapa() != null) {
            audio.setCapa(arquivoService.buscaArquivo(audio.getCapa().getId()));
        }

        if (audio.getCapa() != null && !audio.getCapa().isUsed()) {
            if (entidade.getCapa() != null) {
                arquivoService.registraDesuso(entidade.getCapa().getId());
            }

            arquivoService.registraUso(audio.getCapa().getId());
        } else if (audio.getCapa() == null && entidade.getCapa() != null) {
            arquivoService.registraDesuso(entidade.getCapa().getId());
        }

        audio.setAudio(arquivoService.buscaArquivo(audio.getAudio().getId()));

        if (!audio.getAudio().isUsed()) {
            arquivoService.registraDesuso(entidade.getAudio().getId());

            arquivoService.registraUso(audio.getAudio().getId());

            File file = EntityFileManager.get(audio.getAudio(), "dados");

            audio.setTamamnhoArquivo(file.length());

            try {
                Mp3File mp3 = new Mp3File(file);
                audio.setTempoAudio(mp3.getLengthInSeconds());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        return daoService.update(audio);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public void removeAudio(Long audio) {
        Audio entidade = daoService.find(Audio.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), audio));

        arquivoService.registraDesuso(entidade.getAudio().getId());

        daoService.delete(Audio.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), audio));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public Audio buscaAudio(Long audio) {
        return daoService.find(Audio.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), audio));
    }

    @Override
    @AcessoMarker(Funcionalidade.AUDIOS)
    public BuscaPaginadaAudioDTO buscaTodos(FiltroAudioDTO filtro) {
        BuscaPaginadaDTO<Audio> resultado = daoService.findWith(new FiltroAudio(sessaoBean.getChaveIgreja(), filtro));

        BuscaPaginadaAudioDTO busca = new BuscaPaginadaAudioDTO(
                resultado.getResultados(), resultado.getTotalResultados(),
                filtro.getPagina(), filtro.getTotal()
        );

        if (filtro.getCategoria() != null) {
            busca.setCategoria(daoService.find(CategoriaAudio.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), filtro.getCategoria())));
        }

        return busca;
    }

    @Override
    public BuscaPaginadaDTO<GaleriaDTO> buscaGaleriasFotos(Integer pagina) {
        return flickrService.buscaGaleriaFotos(sessaoBean.getChaveIgreja(), pagina);
    }

    @Override
    @AcessoMarker(Funcionalidade.GALERIA_FOTOS)
    public BuscaPaginadaDTO<FotoDTO> buscaFotos(FiltroFotoDTO filtro) {
        return flickrService.buscaFotos(sessaoBean.getChaveIgreja(), filtro);
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public ConfiguracaoFlickrIgrejaDTO buscaConfiguracaoFlickr() {
        return paramService.buscaConfiguracaoFlickr(sessaoBean.getChaveIgreja());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public String buscaURLAutenticacaoFlickr() throws IOException {
        return flickrService.buscaURLAutenticacaoFlickr(sessaoBean.getChaveIgreja(),
                MessageFormat.format(ResourceBundleUtil._default()
                        .getPropriedade("OAUTH_FLICKR_REDIRECT_URL"), sessaoBean.getChaveIgreja()));
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public void iniciaConfiguracaoFlickr(String token, String verifier) {
        flickrService.iniciaConfiguracaoFlickr(sessaoBean.getChaveIgreja(), token, verifier);
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public void desvinculaFlickr() {
        flickrService.devinculaFlickr(sessaoBean.getChaveIgreja());
    }

    @Schedule(hour = "*", minute = "0/15", persistent = false)
    public void verificaPagSeguro() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            ConfiguracaoIgrejaDTO configuracao = paramService.buscaConfiguracao(igreja.getChave());
            if (configuracao != null && configuracao.isPagSeguroConfigurado()) {
                List<String> referencias = daoService.findWith(QueryAdmin.REFERENCIAS_INSCRICOES_PENDENTES.create(igreja.getChave()));

                for (String referencia : referencias) {
                    atualizaSituacaoPagSeguro(referencia, configuracao);
                }
            }
        }
    }

    public void atualizaSituacaoPagSeguro(String referencia, ConfiguracaoIgrejaDTO configuracao) {
        switch (pagSeguroService.getStatusPagamento(referencia, configuracao)) {
            case PAGO: {
                List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.INSCRICOES_POR_REFERENCIA.create(referencia));

                if (!inscricoes.isEmpty()) {
                    Membro membro = inscricoes.get(0).getMembro();
                    Evento evento = inscricoes.get(0).getEvento();
                    Institucional institucional = daoService.find(Institucional.class, evento.getIgreja().getChave());
                    BigDecimal total = BigDecimal.ZERO;

                    for (InscricaoEvento inscricao : inscricoes) {
                        inscricao.confirmada();
                        daoService.update(inscricao);
                        total = total.add(inscricao.getValor());
                    }

                    Locale locale = Locale.forLanguageTag(evento.getIgreja().getLocale());
                    NumberFormat nformat = NumberFormat.getCurrencyInstance(locale);

                    notificacaoService.sendNow(
                            mensagemBuilder.email(
                                    evento.getIgreja(),
                                    TipoParametro.EMAIL_SUBJECT_CONFIRMAR_INSCRICAO,
                                    TipoParametro.EMAIL_BODY_CONFIRMAR_INSCRICAO,
                                    membro.getNome(),
                                    evento.getNome(),
                                    nformat.format(total),
                                    institucional.getEmail()
                            ),
                            new FiltroEmailDTO(evento.getIgreja(), membro.getId()));


                    scheduleRelatoriosInscritos(evento);
                }

            }
            break;
            case CANCELADO: {
                List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.INSCRICOES_POR_REFERENCIA.create(referencia));
                for (InscricaoEvento inscricao : inscricoes) {
                    inscricao.cancelada();
                    daoService.update(inscricao);
                    scheduleRelatoriosInscritos(inscricao.getEvento());
                }
                break;
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaVersiculos() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer hora = cal.get(Calendar.HOUR_OF_DAY);

            VersiculoDiario atual = daoService.findWith(QueryAdmin.VERSICULOS_POR_STATUS.createSingle(igreja.getId(), StatusVersiculoDiario.ATIVO));
            if (atual == null || !DateUtil.equalsSemHoras(atual.getUltimoEnvio(), new Date())) {
                if (atual != null) {
                    atual.habilitado();
                    atual = daoService.update(atual);
                }

                VersiculoDiario versiculo = daoService.findWith(QueryAdmin.SORTEIA_VERSICULO.createSingle(igreja.getId()));
                if (versiculo != null) {
                    versiculo.ativo();
                    atual = daoService.update(versiculo);
                }
            }

            String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_VERSICULO_DIARIO);

            if (atual != null && atual.isAtivo()) {
                for (HorasEnvioNotificacao hev : HorasEnvioNotificacao.values()) {
                    if (hev.getHoraInt().equals(hora)) {
                        enviaPush(new FiltroDispositivoNotificacaoDTO(igreja, hev),
                                titulo, atual.getVersiculo(), TipoNotificacao.VERSICULO, true);
                        break;
                    }
                }
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaLembreteLeituraBiblica() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer hora = cal.get(Calendar.HOUR_OF_DAY);

            for (HorasEnvioNotificacao hev : HorasEnvioNotificacao.values()) {
                if (hev.getHoraInt().equals(hora)) {
                    String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_LEMBRETE_LEITURA_BIBLICA);

                    FiltroPlanoLeituraBiblicaDTO filtro = new FiltroPlanoLeituraBiblicaDTO();
                    BuscaPaginadaDTO<PlanoLeituraBiblica> busca;
                    do {
                        busca = daoService.findWith(new FiltroPlanoLeituraBiblica(igreja.getId(), false, filtro));

                        for (PlanoLeituraBiblica plano : busca) {
                            DiaLeituraBiblica dia = daoService.findWith(QueryAdmin.DIA_PLANO.createSingle(igreja.getId(), plano.getId(), cal.getTime()));
                            if (dia != null && !StringUtil.isEmpty(dia.getDescricao())) {
                                enviaPush(new FiltroDispositivoNotificacaoDTO(igreja, hev, plano.getId()),
                                        titulo, dia.getDescricao(), TipoNotificacao.PLANO_LEITURA, false);
                            }
                        }

                        filtro.setPagina(filtro.getPagina() + 1);
                    } while (busca.isHasProxima());

                    break;
                }
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaParabensAniversario() {
        LOGGER.info("Iniciando envio de notificações de aniversário.");

        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());

        LOGGER.info(igrejas.size() + " igrejas encontradas para envio de notificações de aniversário.");

        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer hora = cal.get(Calendar.HOUR_OF_DAY);

            if (hora.equals(12)) {
                LOGGER.info("Prepara envio de notificações de aniversário para " + igreja.getChave());

                List<Membro> aniversariantes = daoService.findWith(QueryAdmin.ANIVERSARIANTES.create(igreja.getChave()));

                for (Membro membro : aniversariantes) {
                    String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_ANIVERSARIO);

                    String texto = MessageFormat.format(
                            (String) paramService.get(igreja.getChave(), TipoParametro.PUSH_BODY_ANIVERSARIO),
                            membro.getNome(), igreja.getNome());

                    enviaPush(new FiltroDispositivoNotificacaoDTO(igreja, membro.getId()), titulo, texto, TipoNotificacao.ANIVERSARIO, false);
                }
            } else {
                LOGGER.info(igreja.getChave() + " fora do horário para envio de notificações de aniversário");
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaNotificacoesPublicacoes() {
        LOGGER.info("Iniciando envio de notificações de publicações.");

        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS_COM_PUBLICACOES_A_DIVULGAR.create());

        LOGGER.info(igrejas.size() + " igrejas encontradas para envio de notificações de publicações.");

        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de publicações para " + igreja.getChave());

                Boletim publicacao = daoService.findWith(QueryAdmin.PUBLICACAO_A_DIVULGAR_POR_IGREJA
                        .createSingle(igreja.getChave()));

                String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_PUBLICACAO);

                String texto = MessageFormat.format(
                        (String) paramService.get(igreja.getChave(), TipoParametro.PUSH_BODY_PUBLICACAO),
                        igreja.getNome(), publicacao.getTitulo());

                enviaPush(new FiltroDispositivoNotificacaoDTO(igreja), titulo, texto, TipoNotificacao.PUBLICACAO, false);

                daoService.execute(QueryAdmin.UPDATE_PUBLICACOES_NAO_DIVULGADOS.create(igreja.getChave()));
            } else {
                LOGGER.info("Hora fora do limite de envio de notificações de publicações para " + igreja.getChave());
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaNotificacoesDevocionarios() {
        LOGGER.info("Iniciando envio de notificações de devocionário.");

        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS_COM_DEVOCIONARIO_A_DIVULGAR.create());

        LOGGER.info(igrejas.size() + " igrejas encontradas para envio de notificações de devocionário.");

        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            for (HorasEnvioNotificacao hora : HorasEnvioNotificacao.values()) {
                if (hora.getHoraInt().equals(horaAtual)) {
                    LOGGER.info("Preparando envio de notificações de devocionário para " + igreja.getChave());

                    String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_DEVOCIONARIO);

                    String texto = MessageFormat.format(
                            (String) paramService.get(igreja.getChave(), TipoParametro.PUSH_BODY_DEVOCIONARIO),
                            igreja.getNome());

                    enviaPush(FiltroDispositivoNotificacaoDTO.builder()
                                    .igreja(igreja)
                                    .hora(hora)
                                    .devocionario(true)
                                    .build(),
                            titulo, texto, TipoNotificacao.DEVOCIONARIO, false);
                }
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaNotificacoesBoletins() {
        LOGGER.info("Iniciando envio de notificações de boletins.");

        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS_COM_BOLETINS_A_DIVULGAR.create());

        LOGGER.info(igrejas.size() + " igrejas encontradas para envio de notificações de boletins.");

        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de boletins para " + igreja.getChave());

                Boletim boletim = daoService.findWith(QueryAdmin.BOLETIM_A_DIVULGAR_POR_IGREJA
                        .createSingle(igreja.getChave()));

                String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_BOLETIM);

                String texto = MessageFormat.format(
                        (String) paramService.get(igreja.getChave(), TipoParametro.PUSH_BODY_BOLETIM),
                        igreja.getNome(), boletim.getTitulo());

                enviaPush(new FiltroDispositivoNotificacaoDTO(igreja), titulo, texto, TipoNotificacao.BOLETIM, false);

                daoService.execute(QueryAdmin.UPDATE_BOLETINS_NAO_DIVULGADOS.create(igreja.getChave()));
            } else {
                LOGGER.info("Hora fora do limite de envio de notificações de boletins para " + igreja.getChave());
            }
        }
    }

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    public void verificaProcessamentosDemorados() {
        LOGGER.info("Iniciando verificação de processamentos de PDFs demorando");

        // Boletins
        List<Boletim> boletins = daoService.findWith(QueryAdmin.BOLETINS_PROCESSANDO.create());

        List<Boletim> boletinsEmpacados = new ArrayList<>();
        for (Boletim boletim : boletins) {
            if (boletim.getUltimaAlteracao().before(DateUtil.decrementaMinutos(DateUtil.getDataAtual(), 15))) {
                boletinsEmpacados.add(boletim);
            }
        }

        if (!boletinsEmpacados.isEmpty()) {

        }

        // Estudos
        List<Estudo> estudos = daoService.findWith(QueryAdmin.ESTUDOS_PROCESSANDO.create());

        List<Estudo> estudosEmpacados = new ArrayList<>();
        for (Estudo estudo : estudos) {
            if (estudo.getUltimaAlteracao().before(DateUtil.decrementaMinutos(DateUtil.getDataAtual(), 15))) {
                estudosEmpacados.add(estudo);
            }
        }

        // Devocionários
        List<DiaDevocionario> devocionarios = daoService.findWith(QueryAdmin.DEVOCIONARIOS_PROCESSANDO.create());

        List<DiaDevocionario> devocionariosEmpacados = new ArrayList<>();
        for (DiaDevocionario devocionario : devocionarios) {
            if (devocionario.getUltimaAlteracao().before(DateUtil.decrementaMinutos(DateUtil.getDataAtual(), 15))) {
                devocionariosEmpacados.add(devocionario);
            }
        }

        // Cifras
        List<Cifra> cifras = daoService.findWith(QueryAdmin.CIFRAS_PROCESSANDO.create());

        List<Cifra> cifrasEmpacadas = new ArrayList<>();
        for (Cifra cifra : cifras) {
            if (cifra.getUltimaAlteracao().before(DateUtil.decrementaMinutos(DateUtil.getDataAtual(), 15))) {
                cifrasEmpacadas.add(cifra);
            }
        }

        if (!boletinsEmpacados.isEmpty() ||
                !estudosEmpacados.isEmpty() ||
                !devocionariosEmpacados.isEmpty() ||
                !cifrasEmpacadas.isEmpty()) {
            StringBuilder mensagem = new StringBuilder("Existem PDFs travados no processamento: <br/><br/>");

            if (!boletinsEmpacados.isEmpty()) {
                mensagem.append("BOLETINS: <br/>");

                for (Boletim boletim : boletinsEmpacados) {
                    mensagem.append(boletim.getChaveIgreja().toUpperCase())
                            .append(" ").append(boletim.getId()).append("<br/>");
                }

                mensagem.append("<br/>");
            }

            if (!estudosEmpacados.isEmpty()) {
                mensagem.append("ESTUDOS: <br/>");

                for (Estudo estudo : estudosEmpacados) {
                    mensagem.append(estudo.getChaveIgreja().toUpperCase())
                            .append(" ").append(estudo.getId()).append("<br/>");
                }

                mensagem.append("<br/>");
            }

            if (!cifrasEmpacadas.isEmpty()) {
                mensagem.append("CIFRAS: <br/>");

                for (Cifra cifra : cifrasEmpacadas) {
                    mensagem.append(cifra.getChaveIgreja().toUpperCase())
                            .append(" ").append(cifra.getId()).append("<br/>");
                }

                mensagem.append("<br/>");
            }

            if (!devocionariosEmpacados.isEmpty()) {
                mensagem.append("DEVOCIONÁRIO: <br/>");

                for (DiaDevocionario devocionario : devocionariosEmpacados) {
                    mensagem.append(devocionario.getChaveIgreja().toUpperCase())
                            .append(" ").append(devocionario.getId()).append("<br/>");
                }

                mensagem.append("<br/>");
            }

            EmailUtil.alertAdm(mensagem.toString(), "URGENTE! PDFs TRAVADOS!");
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaNotificacoesEstudos() {
        LOGGER.info("Iniciando envio de notificações de estudos.");

        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS_COM_ESTUDOS_A_DIVULGAR.create());

        LOGGER.info(igrejas.size() + " igrejas encontrada para notificação de estudos.");

        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de estudo para " + igreja.getChave());

                Estudo estudo = daoService.findWith(QueryAdmin.ESTUDO_A_DIVULGAR_POR_IGREJA
                        .createSingle(igreja.getChave()));

                String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_ESTUDO);

                String texto = MessageFormat.format(
                        (String) paramService.get(igreja.getChave(), TipoParametro.PUSH_BODY_ESTUDO),
                        igreja.getNome(), estudo.getCategoria() != null ? estudo.getCategoria().getNome() : "", estudo.getTitulo());

                enviaPush(new FiltroDispositivoNotificacaoDTO(igreja), titulo, texto, TipoNotificacao.ESTUDO, false);

                daoService.execute(QueryAdmin.UPDATE_ESTUDOS_NAO_DIVULGADOS.create(igreja.getChave()));
            } else {
                LOGGER.info(igreja.getChave() + " fora do horário para envio de notiicações de estudos.");
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaNotificacoesNoticias() {
        LOGGER.info("Iniciando envio de notificações de notícia.");

        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS_COM_NOTICIAS_A_DIVULGAR.create());

        LOGGER.info(igrejas.size() + " igrejas encontrada para notificação de notícias.");

        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de notícia para " + igreja.getChave());

                Noticia noticia = daoService.findWith(QueryAdmin.NOTICIA_A_DIVULGAR_POR_IGREJA
                        .createSingle(igreja.getChave()));

                String titulo = paramService.get(igreja.getChave(), TipoParametro.PUSH_TITLE_NOTICIA);

                String texto = MessageFormat.format(
                        (String) paramService.get(igreja.getChave(), TipoParametro.PUSH_BODY_NOTICIA),
                        igreja.getNome(), noticia.getTitulo());

                enviaPush(new FiltroDispositivoNotificacaoDTO(igreja), titulo, texto, TipoNotificacao.NOTICIA, false);

                daoService.execute(QueryAdmin.UPDATE_NOTICIAS_NAO_DIVULGADAS.create(igreja.getChave()));
            } else {
                LOGGER.info(igreja.getChave() + " fora do horário para envio de notiicações de notícias.");
            }
        }
    }

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    public void enviaNotificacoesYouTubeAoVivo() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            ConfiguracaoYouTubeIgrejaDTO config = paramService.buscaConfiguracaoYouTube(igreja.getChave());
            if (config.isConfigurado()) {
                try {
                    List<VideoDTO> streamings = googleService.buscaStreamsAtivosYouTube(igreja.getChave());

                    for (VideoDTO video : streamings) {
                        if (!Persister.file(NotificacaoYouTubeAoVivo.class, video.getId()).exists()) {
                            Persister.save(new NotificacaoYouTubeAoVivo(video), video.getId());

                            try {
                                String titulo = config.getTituloAoVivo();

                                String texto = MessageFormat.format(config.getTextoAoVivo(), video.getTitulo());

                                enviaPush(new FiltroDispositivoNotificacaoDTO(igreja, true), titulo, texto, TipoNotificacao.YOUTUBE, false);
                            } catch (Exception e) {
                                Persister.remove(NotificacaoYouTubeAgendado.class, video.getId());
                                throw e;
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(AppServiceImpl.class.getName()).log(Level.SEVERE, "Erro ao verificar vídeos ao vivo para " + igreja.getChave(), e);
                }
            }
        }
    }

    @Schedule(hour = "*", persistent = false)
    public void enviaNotificacoesYouTubeAgendados() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            ConfiguracaoYouTubeIgrejaDTO config = paramService.buscaConfiguracaoYouTube(igreja.getChave());

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (config.isConfigurado()) {
                try {
                    List<VideoDTO> streamings = googleService.buscaStreamsAgendadosYouTube(igreja.getChave());

                    for (VideoDTO video : streamings) {
                        if (!Persister.file(NotificacaoYouTubeAgendado.class, video.getId()).exists() &&
                                DateUtil.equalsSemHoras(DateUtil.getDataAtual(), video.getAgendamento()) &&
                                // Verifica se está em horário útil para fazer a notificação
                                horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {

                            Persister.save(new NotificacaoYouTubeAgendado(video), video.getId());

                            try {
                                String horario = mensagemBuilder.formataHora(video.getAgendamento(), igreja.getLocale(), igreja.getTimezone());

                                String titulo = MessageFormat.format(config.getTituloAoVivo(), video.getTitulo(), horario);

                                String texto = MessageFormat.format(config.getTextoAoVivo(),
                                        video.getTitulo(), horario);

                                enviaPush(new FiltroDispositivoNotificacaoDTO(igreja, true), titulo, texto, TipoNotificacao.YOUTUBE, false);
                            } catch (Exception e) {
                                Persister.remove(NotificacaoYouTubeAgendado.class, video.getId());
                                throw e;
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(AppServiceImpl.class.getName()).log(Level.SEVERE, "Erro ao verificar vídeos agendados para " + igreja.getChave(), e);
                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificacaoYouTubeAoVivo {
        private VideoDTO video;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificacaoYouTubeAgendado {
        private VideoDTO video;
    }

    private void enviaPush(FiltroDispositivoNotificacaoDTO filtro, String titulo, String mensagem, TipoNotificacao tipo, boolean compartilhavel) {
        notificacaoService.sendNow(new MensagemPushDTO(titulo, mensagem, null, null, null,
                new QueryParameters("tipo", tipo).set("compartilhavel", compartilhavel)), filtro);
    }


}
