/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.servidor;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.dao.QueryParameters;
import br.gafs.exceptions.ServiceException;
import br.gafs.file.EntityFileManager;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.pocket.corporate.dao.*;
import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.entity.*;
import br.gafs.pocket.corporate.entity.domain.*;
import br.gafs.pocket.corporate.exception.ValidationException;
import br.gafs.pocket.corporate.security.*;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.service.ArquivoService;
import br.gafs.pocket.corporate.service.MensagemService;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.pocket.corporate.servidor.flickr.FlickrService;
import br.gafs.pocket.corporate.servidor.google.GoogleService;
import br.gafs.pocket.corporate.servidor.pagseguro.PagSeguroService;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoBoletim;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoDocumento;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.pocket.corporate.servidor.relatorio.RelatorioDocumento;
import br.gafs.pocket.corporate.servidor.relatorio.RelatorioInscritos;
import br.gafs.pocket.corporate.util.MensagemUtil;
import br.gafs.pocket.corporate.util.Persister;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringEscapeUtils;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
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
    private MensagemService notificacaoService;

    @EJB
    private PagSeguroService pagSeguroService;

    @EJB
    private GoogleService googleService;

    @EJB
    private ParametroService paramService;

    @EJB
    private ProcessamentoService processamentoService;

    @EJB
    private FlickrService flickrService;

    @Inject
    private SessaoBean sessaoBean;

    @Override
    @AllowAdmin
    public StatusAdminDTO buscaStatus(){
        StatusAdminDTO status = new StatusAdminDTO();
        status.setMensagemDia(buscaMensagemDia());

        if (sessaoBean.temPermissao(Funcionalidade.CONSULTAR_CONTATOS_COLABORADORES)){
            Number contatos = daoService.findWith(new FiltroContatoColaborador(null, sessaoBean.getChaveEmpresa(),
                    new FiltroContatoColaboradorDTO(null, null, Arrays.asList(StatusContatoColaborador.PENDENTE), 1, 10)).getCountQuery());

            if (contatos.intValue() > 0){
                status.addNotificacao("mensagens.MSG-036",
                        new QueryParameters("quantidade", contatos));
            }
        }

        if (sessaoBean.temPermissao(Funcionalidade.MANTER_EVENTOS)){
            // TODO verificar a quantidade de inscrições pendentes em eventos
        }

        return status;
    }

    @Override
    public BuscaPaginadaDTO<NotificationSchedule> buscaNotificacoes(FiltroNotificacoesDTO filtro) {
        BuscaPaginadaDTO<NotificationSchedule> busca = daoService.findWith(new FiltroNotificacoes(sessaoBean.getChaveEmpresa(),
                sessaoBean.getChaveDispositivo(), sessaoBean.getIdColaborador(), filtro));
        
        if (filtro.getPagina().equals(1)){
            notificacaoService.marcaNotificacoesComoLidas(
                    sessaoBean.getChaveEmpresa(),
                    sessaoBean.getChaveDispositivo(),
                    sessaoBean.getIdColaborador()
            );
        }
        
        return busca;
    }

    @Override
    public void clearNotificacoes(List<Long> excecoes){
        if (excecoes == null || excecoes.isEmpty()) {
            // Evita erros de SQL por causa de lista vazia
            excecoes = Arrays.asList(-1L);
        }

        if (sessaoBean.getIdColaborador() != null){
            daoService.execute(QueryNotificacao.CLEAR_NOTIFICACOES_COLABORADOR.
                    create(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador(), excecoes));
        }

        daoService.execute(QueryNotificacao.CLEAR_NOTIFICACOES_DISPOSITIVO.
                create(sessaoBean.getChaveEmpresa(), sessaoBean.getChaveDispositivo(), excecoes));
    }
    
    @Override
    public void removeNotificacao(Long notificacao){
        SentNotification sn = daoService.find(SentNotification.class, new SentNotificationId(sessaoBean.getChaveDispositivo(), notificacao));
        
        if (sn != null && (sn.getColaborador() == null || sn.getColaborador().equals(sessaoBean.getIdColaborador()))){
            daoService.delete(SentNotification.class, sn.getId());
        }

        if (sessaoBean.getIdColaborador() != null){
            List<SentNotification> sns = daoService.findWith(QueryNotificacao.NOTIFICACAO_COLABORADOR.
                    create(notificacao, sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador()));

            for (SentNotification sn0 : sns){
                daoService.delete(SentNotification.class, sn0.getId());
            }
        }
    }
    
    @Override
    @AllowAdmin
    @AllowColaborador
    public List<ReleaseNotes> buscaReleaseNotes(TipoVersao tipo) {
        return daoService.findWith(QueryAdmin.RELEASE_NOTES.create(tipo));
    }
    
    private MensagemDia buscaMensagemDia(){
        return daoService.findWith(QueryAdmin.MENSAGEM_DIAS_POR_STATUS.
                createSingle(sessaoBean.getChaveEmpresa(), StatusMensagemDia.ATIVO));
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public List<Colaborador> buscaGerentes() {
        return daoService.findWith(QueryAdmin.GERENTES_ATIVOS.
                create(sessaoBean.getChaveEmpresa()));
    }
    
    @Audit
    @Override
    public Chamado solicita(Chamado chamado) {
        if (sessaoBean.isAdmin()){
            chamado.setTipo(TipoChamado.SUPORTE);
            
            if (sessaoBean.getIdColaborador() == null ||
                    !sessaoBean.temPermissao(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)){
                throw new ServiceException("mensagens.MSG-403");
            }
        }else if (chamado.isSuporte()){
            throw new ServiceException("mensagens.MSG-403");
        }

        chamado.setDispositivoSolicitante(daoService.find(Dispositivo.class, sessaoBean.getChaveDispositivo()));
        chamado = daoService.create(chamado);
        
        EmailUtil.sendMail(
                MessageFormat.format(ResourceBundleUtil._default().getPropriedade("CHAMADO_MESSAGE"),
                        chamado.getDescricao(), chamado.getEmpresaSolicitante().getNome(),
                        chamado.getNomeSolicitante(), chamado.getEmailSolicitante(),
                        chamado.getDispositivoSolicitante().getUuid(),
                        chamado.getDispositivoSolicitante().getVersao()),
                MessageFormat.format(ResourceBundleUtil._default().getPropriedade("CHAMADO_SUBJECT"),
                        chamado.getEmpresaSolicitante().getChave().toUpperCase(),
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
        if (chamado == null || !chamado.getEmpresaSolicitante().getChave().equals(sessaoBean.getChaveEmpresa()) ||
                (chamado.isSuporte() && (sessaoBean.getIdColaborador() == null ||
                !sessaoBean.temPermissao(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)))){
            throw new ServiceException("mensagens.MSG-403");
        }
        return chamado;
    }
    
    @Override
    public BuscaPaginadaDTO<Chamado> busca(FiltroChamadoDTO filtro) {
        return daoService.findWith(new FiltroChamado(sessaoBean.getChaveEmpresa(),
                sessaoBean.getChaveDispositivo(), sessaoBean.getIdColaborador(), sessaoBean.isAdmin(), filtro));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_COLABORADORES)
    public Colaborador cadastra(Colaborador colaborador) {
        colaborador.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));

        if (colaborador.getFoto() != null) {
            arquivoService.registraUso(colaborador.getFoto().getId());
            colaborador.setFoto(arquivoService.buscaArquivo(colaborador.getFoto().getId()));
        }

        if (colaborador.getLotacao() != null) {
            colaborador.setLotacao(daoService.find(LotacaoColaborador.class,
                    new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), colaborador.getLotacao().getId())));
        } else {
            colaborador.setLotacao(null);
        }

        return daoService.create(colaborador);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_COLABORADORES)
    public LotacaoColaborador cadastra(LotacaoColaborador lotacao) {
        lotacao.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        return daoService.create(lotacao);
    }

    @Override
    @AllowAdmin({
            Funcionalidade.MANTER_COLABORADORES,
            Funcionalidade.ENVIAR_NOTIFICACOES
    })
    public List<LotacaoColaborador> buscaLotacoesColaborador() {
        return daoService.findWith(QueryAdmin.LOTACAO_COLABORADOR.create(sessaoBean.getChaveEmpresa()));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_COLABORADORES)
    public Colaborador darAcessoColaborador(Long colaborador) {
        Colaborador entidade = buscaColaborador(colaborador);
        
        if (entidade.getId().equals(sessaoBean.getIdColaborador())) {
            throw new ServiceException("mensagens.MSG-015");
        }
        
        boolean gerarSenha = entidade.isSenhaUndefined();
        
        entidade.colaborador();
        entidade = daoService.update(entidade);
        
        if (gerarSenha){
            String senha = SenhaUtil.geraSenha(8);
            
            entidade.setSenha(SenhaUtil.encryptSHA256(senha));
            
            entidade = daoService.update(entidade);
            
            String subject = MensagemUtil.getMensagem("email.dar_acesso.subject", entidade.getEmpresa().getLocale());
            String title = MensagemUtil.getMensagem("email.dar_acesso.message.title", entidade.getEmpresa().getLocale(),
                    entidade.getNome());
            String text = MensagemUtil.getMensagem("email.dar_acesso.message.text", entidade.getEmpresa().getLocale(),
                    entidade.getEmpresa().getNomeAplicativo());
            
            notificacaoService.sendNow(
                    MensagemUtil.email(recuperaInstitucional(), subject,
                            new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, "javascript:void(0)", senha), Collections.EMPTY_LIST)),
                    new FiltroEmailDTO(entidade.getEmpresa(), entidade.getId()));
        }
        
        return entidade;
    }
    
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public List<Funcionalidade> getFuncionalidadesHabilitadasAplicativo() {
        return daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()).getFuncionalidadesAplicativo();
    }
    
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public List<Funcionalidade> getFuncionalidadesAplicativo() {
        return daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()).getPlano().getFuncionalidadesColaborador();
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public void salvaFuncionalidadesHabilitadasAplicativo(List<Funcionalidade> funcionalidades) {
        Empresa empresa = daoService.find(Empresa.class, sessaoBean.getChaveEmpresa());
        empresa.setFuncionalidadesAplicativo(funcionalidades);
        daoService.update(empresa);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_COLABORADORES)
    public Colaborador retiraAcessoColaborador(Long colaborador) {
        Colaborador entidade = buscaColaborador(colaborador);
        
        if (entidade.getId().equals(sessaoBean.getIdColaborador())) {
            throw new ServiceException("mensagens.MSG-015");
        }
        
        entidade.contato();
        return daoService.update(entidade);
    }
    
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_COLABORADORES)
    public Acesso buscaAcessoAdmin(Long colaborador) {
        return daoService.find(Acesso.class, new AcessoId(new RegistroEmpresaId(
                sessaoBean.getChaveEmpresa(), colaborador)));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_COLABORADORES)
    public void removeColaborador(Long colaborador) {
        Colaborador entidade = buscaColaborador(colaborador);
        
        if (!entidade.isColaborador()){
            entidade.exclui();
            daoService.update(entidade);
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_COLABORADORES)
    public void redefinirSenha(Long colaborador) {
        Colaborador entidade = buscaColaborador(colaborador);

        if (entidade.isColaborador()){
            String senha = SenhaUtil.geraSenha(8);

            entidade.setSenha(SenhaUtil.encryptSHA256(senha));
            daoService.update(entidade);

            String subject = MensagemUtil.getMensagem("email.nova_senha.subject",
                    entidade.getEmpresa().getLocale());
            String title = MensagemUtil.getMensagem("email.nova_senha.message.title",
                    entidade.getEmpresa().getLocale(), entidade.getNome());
            String text = MensagemUtil.getMensagem("email.nova_senha.message.text",
                    entidade.getEmpresa().getLocale(), entidade.getEmpresa().getNomeAplicativo());

            notificacaoService.sendNow(
                    MensagemUtil.email(daoService.find(Institucional.class, entidade.getEmpresa().getChave()), subject,
                            new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, "javascript:void(0)", senha), Collections.EMPTY_LIST)),
                    new FiltroEmailDTO(entidade.getEmpresa(), entidade.getId()));
        }
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
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_COLABORADORES)
    public Acesso darAcessoAdmin(Long colaborador, List<Perfil> perfis) {
        Colaborador entidade = buscaColaborador(colaborador);
        
        Acesso acesso = buscaAcessoAdmin(colaborador);
        if (acesso == null) {
            if (!entidade.isColaborador()){
                entidade = darAcessoColaborador(colaborador);
            }
            
            acesso = daoService.create(new Acesso(entidade));
        }
        
        acesso.setPerfis(perfis);

        if (!acesso.possuiPermissao(Funcionalidade.GERENCIAR_ACESSO_COLABORADORES)){
            if (entidade.getId().equals(sessaoBean.getIdColaborador())) {
                throw new ServiceException("mensagens.MSG-015");
            }
        }
        
        return daoService.update(acesso);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_COLABORADORES)
    public void retiraAcessoAdmin(Long colaborador) {
        Colaborador entidade = buscaColaborador(colaborador);
        
        if (entidade.getId().equals(sessaoBean.getIdColaborador())) {
            throw new ServiceException("mensagens.MSG-015");
        }
        
        entidade.retiraAdmin();
        entidade.colaborador();
        daoService.update(entidade);
    }
    
    @Override
    public Empresa buscaPorChave(String chave) {
        return daoService.find(Empresa.class, chave);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_COLABORADORES)
    public Colaborador atualiza(Colaborador colaborador) {

        if (colaborador.getFoto() != null) {
            arquivoService.registraUso(colaborador.getFoto().getId());
            colaborador.setFoto(arquivoService.buscaArquivo(colaborador.getFoto().getId()));
        }

        if (colaborador.getLotacao() != null) {
            colaborador.setLotacao(daoService.find(LotacaoColaborador.class,
                    new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), colaborador.getLotacao().getId())));
        } else {
            colaborador.setLotacao(null);
        }

        return daoService.update(colaborador);
    }
    
    @Override
    @AllowAdmin({
            Funcionalidade.MANTER_COLABORADORES,
            Funcionalidade.GERENCIAR_ACESSO_COLABORADORES
    })
    @AllowColaborador({
            Funcionalidade.CONSULTAR_CONTATOS_EMPRESA
    })
    public Colaborador buscaColaborador(Long colaborador) {
        return daoService.find(Colaborador.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), colaborador));
    }
    
    @Override
    @AllowAdmin({
            Funcionalidade.MANTER_COLABORADORES,
            Funcionalidade.GERENCIAR_ACESSO_COLABORADORES
    })
    @AllowColaborador({
            Funcionalidade.CONSULTAR_CONTATOS_EMPRESA
    })
    public BuscaPaginadaDTO<Colaborador> busca(FiltroColaboradorDTO filtro) {
        return daoService.findWith(new FiltroColaborador(sessaoBean.isAdmin(), sessaoBean.getChaveEmpresa(), filtro));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public Perfil cadastra(Perfil perfil) {
        perfil.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
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
        return daoService.find(Perfil.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), perfil));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public void removePerfil(Long perfil) {
        Perfil entidade = buscaPerfil(perfil);
        daoService.delete(Perfil.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), entidade.getId()));
    }
    
    @Override
    @AllowAdmin({
        Funcionalidade.MANTER_PERFIS,
        Funcionalidade.MANTER_COLABORADORES
    })
    public List<Perfil> buscaPerfis() {
        return daoService.findWith(QueryAdmin.PERFIS.create(sessaoBean.getChaveEmpresa()));
    }
    
    @Schedule(hour = "*", minute = "*/5")
    public void processaBoletins() {
        LOGGER.info("Iniciando processamento de boletins");
        if (BoletimInformativo.locked() < 15){
            List<BoletimInformativo> boletins = daoService.findWith(QueryAdmin.BOLETINS_PROCESSANDO.create());
            LOGGER.info("Quantidade de boletins a processar: " + boletins.size());
            for (BoletimInformativo boletimInformativo : boletins){
                try{
                    if (!BoletimInformativo.locked(new RegistroEmpresaId(boletimInformativo.getChaveEmpresa(), boletimInformativo.getId()))){
                        LOGGER.info("Agendando processamento do boletim " + boletimInformativo.getId());
                        processamentoService.schedule(new ProcessamentoBoletim(boletimInformativo));
                    }else{
                        LOGGER.info("BoletimInformativo já encontra-se em processamento: " + boletimInformativo.getId());
                    }
                }catch(Exception e){
                    LOGGER.log(Level.SEVERE, "Erro ao tentar processar boletim " + boletimInformativo.getId(), e);
                }
            }
        }else{
            LOGGER.info("Limite de processamento paralelos atingido. Aguardando próxima tentativa.");
        }
    }

    @Schedule(hour = "*", minute = "*/5")
    public void processaDocumentos() {
        LOGGER.info("Iniciando processamento de documentos (PDF)");
        if (Documento.locked() < 15){
            List<Documento> documentos = daoService.findWith(QueryAdmin.DOCUMENTOS_PROCESSANDO.create());
            LOGGER.info("Quantidade de documentos a processar: " + documentos.size());
            for (Documento documento : documentos){
                try{
                    if (!Documento.locked(new RegistroEmpresaId(documento.getChaveEmpresa(), documento.getId()))){
                        LOGGER.info("Agendando processamento do documento " + documento.getId());
                        processamentoService.schedule(new ProcessamentoDocumento(documento));
                    }else{
                        LOGGER.info("Documento já encontra-se em processamento: " + documento.getId());
                    }
                }catch(Exception e){
                    LOGGER.log(Level.SEVERE, "Erro ao tentar processar documento " + documento.getId(), e);
                }
            }
        }else{
            LOGGER.info("Limite de processamento paralelos atingido. Aguardando próxima tentativa.");
        }
    }

    @Override
    @AllowAdmin
    public File buscaAjuda(String path) {
        Empresa empresa = daoService.find(Empresa.class, sessaoBean.getChaveEmpresa());
        return new File(new File(new File(ResourceBundleUtil._default().
                getPropriedade("RESOURCES_ROOT"), "ajuda"), empresa.getLocale()), path);
    }
    
    public AppServiceImpl() {
    }
    
    private int trataPaginasPDF(ArquivoPDF pdf) throws IOException {
        return ProcessamentoBoletim.trataPaginasPDF(new ProcessamentoBoletim.TransactionHandler() {
            @Override
            public <T> T transactional(ProcessamentoService.ExecucaoTransacional<T> execucaoTransacional) {
                return execucaoTransacional.execute(daoService);
            }
        }, pdf, -1);
    }
    
    private boolean trataTrocaPDF(final ArquivoPDF pdf) {
        if (!pdf.getPDF().isUsed()) {
            if (pdf.getId() != null){
                ArquivoPDF old = daoService.find(pdf.getClass(), new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), pdf.getId()));
                List<Arquivo> pages = new ArrayList<Arquivo>();
                if (old != null) {
                    arquivoService.registraDesuso(old.getPDF().getId());
                    if (old.getThumbnail() != null){
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
    public BoletimInformativo cadastra(BoletimInformativo boletimInformativo) throws IOException {
        boletimInformativo.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        boletimInformativo.setBoletim(arquivoService.buscaArquivo(boletimInformativo.getBoletim().getId()));
        boletimInformativo.setUltimaAlteracao(DateUtil.getDataAtual());

        if (trataTrocaPDF(boletimInformativo)){
            boletimInformativo.processando();

            processamentoService.schedule(new ProcessamentoBoletim(boletimInformativo));
        }

        return daoService.create(boletimInformativo);
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_PUBLICACOES, Funcionalidade.MANTER_BOLETINS})
    public BoletimInformativo atualiza(BoletimInformativo boletimInformativo) throws IOException {
        boletimInformativo.setBoletim(arquivoService.buscaArquivo(boletimInformativo.getBoletim().getId()));
        boletimInformativo.setUltimaAlteracao(DateUtil.getDataAtual());
        if (trataTrocaPDF(boletimInformativo)){
            boletimInformativo.processando();

            processamentoService.schedule(new ProcessamentoBoletim(boletimInformativo));
        }
        return daoService.update(boletimInformativo);
    }
    
    @Override
    public BoletimInformativo buscaBoletim(Long boletim) {
        return daoService.find(BoletimInformativo.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), boletim));
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_PUBLICACOES, Funcionalidade.MANTER_BOLETINS})
    public void removeBoletim(Long boletim) {
        BoletimInformativo entidade = buscaBoletim(boletim);
        
        if (entidade != null){
            for (Arquivo page : entidade.getPaginas()) {
                arquivoService.registraDesuso(page.getId());
            }
            
            arquivoService.registraDesuso(entidade.getBoletim().getId());
            
            if (entidade.getThumbnail() != null){
                arquivoService.registraDesuso(entidade.getThumbnail().getId());
            }
            
            daoService.delete(BoletimInformativo.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), entidade.getId()));
        }
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_BOLETINS, Funcionalidade.MANTER_PUBLICACOES})
    public BuscaPaginadaDTO<BoletimInformativo> buscaTodos(FiltroBoletimDTO filtro) {
        return daoService.findWith(new FiltroBoletim(sessaoBean.getChaveEmpresa(), sessaoBean.isAdmin(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<BoletimInformativo> buscaPublicados(FiltroBoletimPublicadoDTO filtro) {
        return buscaTodos(filtro);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_DADOS_INSTITUCIONAIS)
    public Institucional atualiza(Institucional institucional) {
        if (institucional.getDivulgacao() != null){
            institucional.setDivulgacao(arquivoService.buscaArquivo(institucional.getDivulgacao().getId()));
            arquivoService.registraUso(institucional.getDivulgacao().getId());
        }
        
        return daoService.update(institucional);
    }
    
    @Override
    public Institucional recuperaInstitucional() {
        Institucional institucional = daoService.find(Institucional.class, sessaoBean.getChaveEmpresa());
        if (institucional == null) {
            institucional = new Institucional(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        }
        return institucional;
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_DOCUMENTOS)
    public CategoriaDocumento cadastra(CategoriaDocumento categoria) {
        categoria.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        return daoService.create(categoria);
    }

    @Override
    public List<CategoriaDocumento> buscaCategoriasDocumento() {
        if (sessaoBean.isAdmin()) {
            return daoService.findWith(QueryAdmin.CATEGORIA_DOCUMENTO.create(sessaoBean.getChaveEmpresa()));
        } else {
            return daoService.findWith(QueryAdmin.CATEGORIA_USADAS_DOCUMENTO.create(sessaoBean.getChaveEmpresa()));
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_DOCUMENTOS)
    public Documento cadastra(Documento documento) {
        documento.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));

        if (documento.getCategoria() != null) {
            documento.setCategoria(daoService.find(CategoriaDocumento.class,
                    new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), documento.getCategoria().getId())));
        }

        documento.setColaborador(buscaColaborador(sessaoBean.getIdColaborador()));

        if (documento.getPDF() != null) {
            documento.setPdf(arquivoService.buscaArquivo(documento.getPDF().getId()));
            if (trataTrocaPDF(documento)){
                documento.processando();

                processamentoService.schedule(new ProcessamentoDocumento(documento));
            }
        } else {
            documento.publicado();
        }

        documento = daoService.create(documento);
        scheduleRelatorioDocumento(documento);

        return documento;
    }
    
    private void scheduleRelatorioDocumento(Documento documento){
        try {
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioDocumento(documento), "pdf"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioDocumento(documento), "xls"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioDocumento(documento), "docx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_DOCUMENTOS)
    public Documento atualiza(Documento documento) {
        documento.setCategoria(daoService.find(CategoriaDocumento.class,
                new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), documento.getCategoria().getId())));

        documento.alterado();

        if (documento.getPDF() != null) {
            documento.setPdf(arquivoService.buscaArquivo(documento.getPDF().getId()));
            if (trataTrocaPDF(documento)){
                documento.processando();

                processamentoService.schedule(new ProcessamentoDocumento(documento));
            }
        } else {
            documento.publicado();
        }

        documento = daoService.update(documento);
        scheduleRelatorioDocumento(documento);

        return documento;
    }
    
    @Override
    public Documento buscaDocumento(Long documento) {
        return daoService.find(Documento.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), documento));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_DOCUMENTOS)
    public void removeDocumento(Long documento) {
        Documento entidade = buscaDocumento(documento);
        daoService.delete(Documento.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), entidade.getId()));
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_DOCUMENTOS)
    public BuscaPaginadaDocumentoDTO buscaTodos(FiltroDocumentoDTO filtro) {
        BuscaPaginadaDTO<Documento> resultado = daoService.findWith(new FiltroDocumento(sessaoBean.getChaveEmpresa(), sessaoBean.isAdmin(), filtro));

        BuscaPaginadaDocumentoDTO documentos = new BuscaPaginadaDocumentoDTO(resultado.getResultados(),
                resultado.getTotalResultados(), filtro.getPagina(), filtro.getTotal());

        if (filtro.getCategoria() != null) {
            documentos.setCategoria(daoService.find(CategoriaDocumento.class,
                    new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), filtro.getCategoria())));
        }

        return documentos;
    }
    
    @Override
    public BuscaPaginadaDocumentoDTO buscaPublicados(FiltroDocumentoPublicadoDTO filtro) {
        return buscaTodos(filtro);
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_NOTICIAS,Funcionalidade.MANTER_CLASSIFICADOS})
    public Noticia cadastra(Noticia noticia) {
        noticia.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        noticia.setAutor(buscaColaborador(sessaoBean.getIdColaborador()));

        if (noticia.getIlustracao() != null) {
            arquivoService.registraUso(noticia.getIlustracao().getId());
            noticia.setIlustracao(arquivoService.buscaArquivo(noticia.getIlustracao().getId()));
        }

        preparaResumo(noticia);
        return daoService.create(noticia);
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_NOTICIAS,Funcionalidade.MANTER_CLASSIFICADOS})
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
        return daoService.find(Noticia.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), noticia));
    }

    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_NOTICIAS,Funcionalidade.MANTER_CLASSIFICADOS})
    public void removeNoticia(Long noticia) {
        Noticia entidade = buscaNoticia(noticia);
        daoService.delete(Noticia.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), entidade.getId()));
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_NOTICIAS,Funcionalidade.MANTER_CLASSIFICADOS})
    public BuscaPaginadaDTO<Noticia> buscaTodos(FiltroNoticiaDTO filtro) {
        return daoService.findWith(new FiltroNoticia(sessaoBean.getChaveEmpresa(), sessaoBean.isAdmin(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<Noticia> buscaPublicados(FiltroNoticiaPublicadaDTO filtro) {
        return buscaTodos(filtro);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.ENVIAR_NOTIFICACOES)
    public void enviar(Notificacao notificacao) {
        notificacao.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        
        if (StringUtil.isEmpty(notificacao.getTitulo())){
            notificacao.setTitulo(MensagemUtil.getMensagem("push.notificacao.title",
                    notificacao.getEmpresa().getLocale(), notificacao.getEmpresa().getNomeAplicativo()));
        }
        
        notificacao = daoService.create(notificacao);
        
        FiltroDispositivoNotificacaoDTO filtro = new FiltroDispositivoNotificacaoDTO(notificacao.getEmpresa());
        filtro.setApenasGerentes(notificacao.isApenasGerentes());

        for (LotacaoColaborador lotacao : notificacao.getLotacoes()) {
            filtro.getLotacoes().add(lotacao.getId());
        }

        enviaPush(filtro, notificacao.getTitulo(), notificacao.getMensagem(), TipoNotificacao.NOTIFICACAO, false);
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ENQUETES)
    @AllowColaborador(Funcionalidade.RESPONDER_ENQUETE)
    public ResultadoEnqueteDTO buscaResultado(Long enquete) {
        Enquete entidade = buscaEnquete(enquete);

        if (!entidade.isEncerrado() && !sessaoBean.isAdmin()) {
            throw new ServiceException("mensagens.MSG-053");
        }

        ResultadoEnqueteDTO dto = new ResultadoEnqueteDTO(entidade);
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
    @AllowAdmin(Funcionalidade.MANTER_ENQUETES)
    public Enquete cadastra(Enquete enquete) {
        enquete.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        preencheRelacionamentos(enquete);
        return daoService.update(enquete);
    }
    
    private void preencheRelacionamentos(Enquete enquete) {
        List<Questao> questoes = new ArrayList<Questao>();
        for (Questao questao : enquete.getQuestoes()) {
            questao.setEnquete(enquete);
            List<Opcao> opcoes = new ArrayList<Opcao>();
            for (Opcao opcao : questao.getOpcoes()) {
                opcao.setQuestao(questao);
                opcoes.add(opcao);
            }
            questao.setOpcoes(opcoes);
            questoes.add(questao);
        }
        enquete.setQuestoes(questoes);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ENQUETES)
    public Enquete atualiza(Enquete enquete) {
        preencheRelacionamentos(enquete);
        return daoService.update(enquete);
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ENQUETES)
    @AllowColaborador(Funcionalidade.RESPONDER_ENQUETE)
    public Enquete buscaEnquete(Long enquete) {
        return daoService.find(Enquete.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), enquete));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ENQUETES)
    public void removeEnquete(Long enquete) {
        Enquete entidade = buscaEnquete(enquete);
        
        if (entidade != null){
            daoService.execute(QueryAdmin.REMOVER_VOTOS.create(sessaoBean.getChaveEmpresa(), enquete));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_OPCAO.create(sessaoBean.getChaveEmpresa(), enquete));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_QUESTAO.create(sessaoBean.getChaveEmpresa(), enquete));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_ENQUETE.create(sessaoBean.getChaveEmpresa(), enquete));
        }
        
        daoService.delete(Enquete.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), entidade.getId()));
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ENQUETES)
    public BuscaPaginadaDTO<Enquete> buscaTodas(FiltroEnqueteDTO filtro) {
        BuscaPaginadaDTO<Object[]> busca = daoService.findWith(new FiltroEnquete(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador(), filtro));

        List<Enquete> enquetes = new ArrayList<>();

        for (Object[] os : busca) {
            Enquete enquete = (Enquete) os[0];
            enquete.setRespondido(((Number) os[1]).intValue() > 0);
            enquetes.add(enquete);
        }

        return new BuscaPaginadaDTO<>(enquetes, busca.getTotalResultados(), busca.getPagina(), filtro.getTotal());
    }
    
    @Override
    @AllowColaborador(Funcionalidade.RESPONDER_ENQUETE)
    public BuscaPaginadaDTO<Enquete> buscaAtivas(FiltroEnqueteAtivaDTO filtro) {
        return buscaTodas(filtro);
    }
    
    @Audit
    @Override
    @AllowColaborador(Funcionalidade.RESPONDER_ENQUETE)
    public void realizarEnquete(RespostaEnquete resposta) {
        daoService.create(resposta);
        daoService.create(new RespostaEnqueteColaborador(resposta.getEnquete(), buscaColaborador(sessaoBean.getIdColaborador())));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONSULTAR_CONTATOS_COLABORADORES)
    public ContatoColaborador atende(Long contatoColaborador) {
        ContatoColaborador entidade = daoService.find(ContatoColaborador.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), contatoColaborador));
        entidade.atende(buscaColaborador(sessaoBean.getIdColaborador()));
        entidade = daoService.update(entidade);
        
        enviaPush(new FiltroDispositivoNotificacaoDTO(entidade.getEmpresa(),
                entidade.getSolicitante().getId()),
                MensagemUtil.getMensagem("push.atendimento_contato_colaborador.title", entidade.getEmpresa().getLocale()),
                MensagemUtil.getMensagem("push.atendimento_contato_colaborador.message", entidade.getEmpresa().getLocale(),
                        MensagemUtil.formataDataHora(entidade.getDataSolicitacao(), entidade.getEmpresa().getLocale(), entidade.getEmpresa().getTimezone())),
                TipoNotificacao.CONTATO_COLABORADOR, false);
        
        return entidade;
    }
    
    @Override
    @AllowAdmin(Funcionalidade.CONSULTAR_CONTATOS_COLABORADORES)
    public BuscaPaginadaDTO<ContatoColaborador> buscaTodos(FiltroContatoColaboradorDTO filtro) {
        return daoService.findWith(new FiltroContatoColaborador(sessaoBean.getIdColaborador(), sessaoBean.getChaveEmpresa(), filtro));
    }
    
    @Override
    @AllowColaborador(Funcionalidade.ENVIAR_CONTATO_COLABORADOR)
    public BuscaPaginadaDTO<ContatoColaborador> buscaMeus(FiltroMeusContatosColaboradorDTO filtro) {
        return buscaTodos(filtro);
    }
    
    @Audit
    @Override
    @AllowColaborador(Funcionalidade.ENVIAR_CONTATO_COLABORADOR)
    public ContatoColaborador realizaContato(ContatoColaborador contato) {
        contato.setSolicitante(buscaColaborador(sessaoBean.getIdColaborador()));
        return daoService.create(contato);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowColaborador(Funcionalidade.REALIZAR_AGENDAMENTO)
    public AgendamentoAtendimento agenda(Long colaborador, Long idHorario, Date data) {
        if (!sessaoBean.isAdmin()) {
            return _agenda(buscaColaborador(sessaoBean.getIdColaborador()), idHorario, data);
        } else {
            return confirma(_agenda(daoService.find(Colaborador.class,
                    new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), colaborador)), idHorario, data).getId());
        }
    }
    
    private AgendamentoAtendimento _agenda(Colaborador colaborador, Long idHorario, Date data) {
        HorarioAtendimento horario = daoService.find(HorarioAtendimento.class, idHorario);
        
        if (!sessaoBean.getChaveEmpresa().equals(horario.getCalendario().getEmpresa().getChave())) {
            throw new ServiceException("mensagens.MSG-604");
        }
        
        AgendamentoAtendimento atendimento = daoService.create(new AgendamentoAtendimento(colaborador, horario, data));
        
        if (!sessaoBean.isAdmin()){
            enviaPush(new FiltroDispositivoNotificacaoDTO(atendimento.getEmpresa(), atendimento.getCalendario().getGerente().getId()),
                    MensagemUtil.getMensagem("push.agendamento.title", atendimento.getEmpresa().getLocale()),
                    MensagemUtil.getMensagem("push.agendamento.message", atendimento.getEmpresa().getLocale(),
                            atendimento.getColaborador().getNome(),
                            MensagemUtil.formataData(atendimento.getDataHoraInicio(),
                                    atendimento.getEmpresa().getLocale(),
                                    atendimento.getEmpresa().getTimezone()),
                            MensagemUtil.formataHora(atendimento.getDataHoraInicio(),
                                    atendimento.getEmpresa().getLocale(),
                                    atendimento.getEmpresa().getTimezone()),
                            MensagemUtil.formataHora(atendimento.getDataHoraFim(),
                                    atendimento.getEmpresa().getLocale(),
                                    atendimento.getEmpresa().getTimezone())),
                    TipoNotificacao.AGENDAMENTO, false);
        }
        
        return atendimento;
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowColaborador(Funcionalidade.REALIZAR_AGENDAMENTO)
    public AgendamentoAtendimento confirma(Long id) {
        AgendamentoAtendimento agendamento = buscaAgendamento(id);
        
        if (!sessaoBean.isAdmin()
                && !agendamento.getCalendario().getGerente().getId().equals(sessaoBean.getIdColaborador())) {
            throw new ServiceException("mensagens.MSG-604");
        }
        
        agendamento.confirmado();
        agendamento = daoService.update(agendamento);
        
        enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getEmpresa(), agendamento.getColaborador().getId()),
                MensagemUtil.getMensagem("push.confirmacao_agendamento.title", agendamento.getEmpresa().getLocale()),
                MensagemUtil.getMensagem("push.confirmacao_agendamento.message", agendamento.getEmpresa().getLocale(),
                        agendamento.getCalendario().getGerente().getNome(),
                        MensagemUtil.formataData(agendamento.getDataHoraInicio(),
                                agendamento.getEmpresa().getLocale(),
                                agendamento.getEmpresa().getTimezone()),
                        MensagemUtil.formataHora(agendamento.getDataHoraInicio(),
                                agendamento.getEmpresa().getLocale(),
                                agendamento.getEmpresa().getTimezone()),
                        MensagemUtil.formataHora(agendamento.getDataHoraFim(),
                                agendamento.getEmpresa().getLocale(),
                                agendamento.getEmpresa().getTimezone())),
                TipoNotificacao.AGENDAMENTO, false);
        
        return agendamento;
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowColaborador(Funcionalidade.REALIZAR_AGENDAMENTO)
    public AgendamentoAtendimento cancela(Long id) {
        AgendamentoAtendimento agendamento = buscaAgendamento(id);
        
        if (!sessaoBean.isAdmin()
                && !agendamento.getColaborador().getId().equals(sessaoBean.getIdColaborador())
                && !agendamento.getCalendario().getGerente().getId().equals(sessaoBean.getIdColaborador())) {
            throw new ServiceException("mensagens.MSG-604");
        }
        
        agendamento.cancelado();
        
        agendamento = daoService.update(agendamento);
        
        if (sessaoBean.isAdmin() ||
                agendamento.getCalendario().getGerente().getId().equals(sessaoBean.getIdColaborador())){
            enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getEmpresa(),
                    agendamento.getColaborador().getId()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_gerente.title", agendamento.getEmpresa().getLocale()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_gerente.message", agendamento.getEmpresa().getLocale(),
                            agendamento.getCalendario().getGerente().getNome(),
                            MensagemUtil.formataData(agendamento.getDataHoraInicio(),
                                    agendamento.getEmpresa().getLocale(),
                                    agendamento.getEmpresa().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraInicio(),
                                    agendamento.getEmpresa().getLocale(),
                                    agendamento.getEmpresa().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraFim(),
                                    agendamento.getEmpresa().getLocale(),
                                    agendamento.getEmpresa().getTimezone())),
                    TipoNotificacao.AGENDAMENTO, false);
        }else{
            enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getEmpresa(),
                    agendamento.getCalendario().getGerente().getId()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_colaborador.title", agendamento.getEmpresa().getLocale()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_colaborador.message", agendamento.getEmpresa().getLocale(),
                            agendamento.getColaborador().getNome(),
                            MensagemUtil.formataData(agendamento.getDataHoraInicio(),
                                    agendamento.getEmpresa().getLocale(),
                                    agendamento.getEmpresa().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraInicio(),
                                    agendamento.getEmpresa().getLocale(),
                                    agendamento.getEmpresa().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraFim(),
                                    agendamento.getEmpresa().getLocale(),
                                    agendamento.getEmpresa().getTimezone())),
                    TipoNotificacao.AGENDAMENTO, false);
        }
        
        return agendamento;
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public AgendamentoAtendimento buscaAgendamento(Long agendamento) {
        return daoService.find(AgendamentoAtendimento.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), agendamento));
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public List<AgendamentoAtendimento> buscaAgendamentos(CalendarioAtendimento calendario, Date dataInicio, Date dataTermino) {
        Date dataAtual = DateUtil.getDataAtual();
        return daoService.findWith(QueryAdmin.AGENDAMENTOS_ATENDIMENTO.
                create(sessaoBean.getChaveEmpresa(), calendario.getId(),
                        dataInicio.before(dataAtual) ? dataAtual : dataInicio, dataTermino));
    }
    
    @Override
    @AllowColaborador(Funcionalidade.REALIZAR_AGENDAMENTO)
    public BuscaPaginadaDTO<AgendamentoAtendimento> buscaMeusAgendamentos(FiltroMeusAgendamentoDTO filtro) {
        return daoService.findWith(new FiltroMeusAgendamentos(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador(), filtro));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public CalendarioAtendimento cadastra(CalendarioAtendimento calendario) {
        calendario.setGerente(buscaColaborador(calendario.getGerente().getId()));
        calendario.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        return daoService.create(calendario);
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public CalendarioAtendimento buscaCalendario(Long calendario) {
        return daoService.find(CalendarioAtendimento.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), calendario));
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
    @AllowColaborador(Funcionalidade.REALIZAR_AGENDAMENTO)
    public List<CalendarioAtendimento> buscaCalendarios() {
        return daoService.findWith(QueryAdmin.CALENDARIOS.create(sessaoBean.getChaveEmpresa()));
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
    @AllowColaborador(Funcionalidade.REALIZAR_AGENDAMENTO)
    public List<EventoAgendaDTO> buscaAgenda(Long idCalendario, Date dataInicio, Date dataTermino) {
        Date dataAtual = DateUtil.incrementaHoras(DateUtil.getDataAtual(), 3);
        
        dataInicio = dataAtual.before(dataInicio) ? dataInicio : dataAtual;
        
        CalendarioAtendimento calendario = buscaCalendario(idCalendario);
        
        List<EventoAgendaDTO> eventos = new ArrayList<EventoAgendaDTO>();
        List<HorarioAtendimento> horarios = daoService.
                findWith(QueryAdmin.HORARIOS_POR_PERIODO.
                        create(idCalendario, dataInicio, dataTermino));
        
        TimeZone timeZone = TimeZone.getTimeZone(calendario.getEmpresa().getTimezone());
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
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public Evento cadastra(Evento evento) {
        evento.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));

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
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioInscritos(evento), "pdf"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioInscritos(evento), "xls"));
            processamentoService.schedule(new ProcessamentoRelatorioCache(new RelatorioInscritos(evento), "docx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
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
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    @AllowColaborador({Funcionalidade.REALIZAR_INSCRICAO_EVENTO})
    public Evento buscaEvento(Long evento) {
        Evento entidade = daoService.find(Evento.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), evento));
        entidade.setVagasRestantes(entidade.getLimiteInscricoes() - ((Number) daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento))).intValue());
        return entidade;
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public void removeEvento(Long evento) {
        Evento entidade = buscaEvento(evento);
        
        entidade.inativo();
        
        daoService.update(entidade);
    }
    
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public BuscaPaginadaDTO<Evento> buscaTodos(FiltroEventoDTO filtro) {
        BuscaPaginadaDTO<Object[]> dados = daoService.findWith(new FiltroEvento(sessaoBean.getChaveEmpresa(), sessaoBean.isAdmin(), filtro));

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
    @AllowColaborador({Funcionalidade.REALIZAR_INSCRICAO_EVENTO})
    public BuscaPaginadaDTO<Evento> buscaFuturos(FiltroEventoFuturoDTO filtro) {
        return buscaTodos(filtro);
    }
    
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public BuscaPaginadaDTO<InscricaoEvento> buscaTodas(Long evento, FiltroInscricaoDTO filtro) {
        return daoService.findWith(new FiltroInscricao(evento, sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador(), filtro));
    }
    
    @Override
    @AllowColaborador({Funcionalidade.REALIZAR_INSCRICAO_EVENTO})
    public BuscaPaginadaDTO<InscricaoEvento> buscaMinhas(Long evento, FiltroMinhasInscricoesDTO filtro) {
        return buscaTodas(evento, filtro);
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public void confirmaInscricao(Long evento, Long inscricao) {
        InscricaoEvento entidade = daoService.find(InscricaoEvento.class,
                new InscricaoEventoId(inscricao, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), evento)));
        
        entidade.confirmada();
        
        daoService.update(entidade);
        
        scheduleRelatoriosInscritos(entidade.getEvento());
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public void cancelaInscricao(Long evento, Long inscricao) {
        InscricaoEvento entidade = daoService.find(InscricaoEvento.class,
                new InscricaoEventoId(inscricao, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), evento)));
        
        entidade.cancelada();
        
        daoService.update(entidade);
        
        scheduleRelatoriosInscritos(entidade.getEvento());
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MENSAGENS_DIA)
    public MensagemDia cadastra(MensagemDia mensagemDia) {
        mensagemDia.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        Number minimo = daoService.findWith(QueryAdmin.MENOR_ENVIO_MENSAGEM_DIAS.createSingle(sessaoBean.getChaveEmpresa()));
        if (minimo != null){
            mensagemDia.setEnvios(minimo.intValue());
        }
        return daoService.create(mensagemDia);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MENSAGENS_DIA)
    public MensagemDia desabilita(Long mensagemDia) {
        MensagemDia entidade = buscaMensagemDia(mensagemDia);
        entidade.desabilitado();
        return daoService.update(entidade);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MENSAGENS_DIA)
    public MensagemDia habilita(Long mensagemDia) {
        MensagemDia entidade = buscaMensagemDia(mensagemDia);
        entidade.habilitado();
        Number minimo = daoService.findWith(QueryAdmin.MENOR_ENVIO_MENSAGEM_DIAS.createSingle(sessaoBean.getChaveEmpresa()));
        if (minimo != null){
            entidade.setEnvios(minimo.intValue());
        }
        return daoService.update(entidade);
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MENSAGENS_DIA)
    public MensagemDia buscaMensagemDia(Long mensagemDia) {
        return daoService.find(MensagemDia.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), mensagemDia));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MENSAGENS_DIA)
    public void removeMensagemDia(Long mensagemDia) {
        MensagemDia entidade = buscaMensagemDia(mensagemDia);
        daoService.delete(MensagemDia.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), entidade.getId()));
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MENSAGENS_DIA)
    public BuscaPaginadaDTO<MensagemDia> busca(FiltroMensagemDiaDTO filtro) {
        return daoService.findWith(new FiltroMensagemDia(sessaoBean.getChaveEmpresa(), filtro));
    }
    
    private boolean temAgendamento(CalendarioAtendimento calendario, Date dti, Date dtf) {
        return daoService.findWith(QueryAdmin.AGENDAMENTO_EM_CHOQUE.createSingle(calendario.getId(), dti, dtf)) != null;
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    @AllowColaborador({Funcionalidade.REALIZAR_INSCRICAO_EVENTO})
    public ResultadoInscricaoDTO realizaInscricao(List<InscricaoEvento> inscricoes) {
        if (!inscricoes.isEmpty()) {
            Evento evento = inscricoes.get(0).getEvento();
            Colaborador colaborador = buscaColaborador(sessaoBean.getIdColaborador());
            
            Number qtde = daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento.getId()));
            if (qtde.intValue() + inscricoes.size() > evento.getLimiteInscricoes()){
                throw new ServiceException("mensagens.MSG-034");
            }
            
            List<InscricaoEvento> cadastradas = new ArrayList<InscricaoEvento>();
            for (InscricaoEvento inscricao : inscricoes) {
                inscricao.setColaborador(colaborador);
                cadastradas.add(daoService.create(inscricao));
            }

            if (sessaoBean.isAdmin()){
                for (InscricaoEvento inscricao : cadastradas) {
                    inscricao.confirmada();
                    daoService.update(inscricao);
                }
            } else if (evento.isComPagamento()) {
                BigDecimal valorTotal = BigDecimal.ZERO;
                
                for (InscricaoEvento inscricao : cadastradas) {
                    valorTotal = valorTotal.add(inscricao.getValor());
                }
                
                ConfiguracaoEmpresaDTO configuracao = buscaConfiguracao();
                if (configuracao != null && configuracao.isHabilitadoPagSeguro()){
                    String referencia = sessaoBean.getChaveEmpresa().toUpperCase() +
                            Long.toString(System.currentTimeMillis(), 36).toUpperCase();
                    
                    PagSeguroService.Pedido pedido = new PagSeguroService.Pedido(referencia,
                            new PagSeguroService.Solicitante(colaborador.getNome(), colaborador.getEmail()));
                    
                    for (InscricaoEvento inscricao : inscricoes){
                        pedido.add(new PagSeguroService.ItemPedido(
                                Long.toString(inscricao.getId(), 36).toUpperCase(),
                                MensagemUtil.getMensagem("pagseguro.inscricao.item", evento.getEmpresa().getLocale(),
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
                    
                    Locale locale = Locale.forLanguageTag(evento.getEmpresa().getLocale());
                    NumberFormat nformat = NumberFormat.getCurrencyInstance(locale);
                    
                    String subject = MensagemUtil.getMensagem("email.pagamento_inscricao.subject", evento.getEmpresa().getLocale());
                    String title = MensagemUtil.getMensagem("email.pagamento_inscricao.message.title", evento.getEmpresa().getLocale(), colaborador.getNome());
                    String text = MensagemUtil.getMensagem("email.pagamento_inscricao.message.text", evento.getEmpresa().getLocale(), evento.getNome(), nformat.format(pedido.getTotal()));
                    String url = MensagemUtil.getMensagem("email.pagamento_inscricao.message.link.url", evento.getEmpresa().getLocale(), checkout);
                    String link = MensagemUtil.getMensagem("email.pagamento_inscricao.message.link.text", evento.getEmpresa().getLocale());
                    
                    notificacaoService.sendNow(
                            MensagemUtil.email(recuperaInstitucional(), subject,
                                    new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, url, link), Collections.EMPTY_LIST)),
                            new FiltroEmailDTO(evento.getEmpresa(), colaborador.getId()));
                    
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
    public ConfiguracaoEmpresaDTO atualiza(ConfiguracaoEmpresaDTO configuracao) {
        ValidationException validation = ValidationException.build();
        
        if (!StringUtil.isEmpty(configuracao.getTituloAniversario()) &&
                configuracao.getTituloAniversario().length() > 30){
            validation.add("tituloAniversario", "mensagens.MSG-010");
        }
        
        if (!StringUtil.isEmpty(configuracao.getTituloBoletim()) &&
                configuracao.getTituloBoletim().length() > 30){
            validation.add("tituloBoletim", "mensagens.MSG-010");
        }
        
        if (!StringUtil.isEmpty(configuracao.getTituloMensagemDia()) &&
                configuracao.getTituloMensagemDia().length() > 30){
            validation.add("tituloMensagemDia", "mensagens.MSG-010");
        }
        
        if (!StringUtil.isEmpty(configuracao.getTextoAniversario()) &&
                configuracao.getTextoAniversario().length() > 150){
            validation.add("textoAniversario", "mensagens.MSG-010");
        }
        
        if (!StringUtil.isEmpty(configuracao.getTextoBoletim()) &&
                configuracao.getTextoBoletim().length() > 150){
            validation.add("textoBoletim", "mensagens.MSG-010");
        }
        
        validation.validate();
        
        paramService.salvaConfiguracao(configuracao, sessaoBean.getChaveEmpresa());
        return buscaConfiguracao();
    }
    
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public ConfiguracaoEmpresaDTO buscaConfiguracao() {
        return paramService.buscaConfiguracao(sessaoBean.getChaveEmpresa());
    }
    
    @Override
    public void verificaPagSeguroPorCodigo(String code) {
        ConfiguracaoEmpresaDTO configuracao = paramService.buscaConfiguracao(sessaoBean.getChaveEmpresa());
        atualizaSituacaoPagSeguro(pagSeguroService.buscaReferenciaPorCodigo(code, configuracao), configuracao);
    }
    
    @Override
    public void verificaPagSeguroPorIdTransacao(String transactionId) {
        ConfiguracaoEmpresaDTO configuracao = paramService.buscaConfiguracao(sessaoBean.getChaveEmpresa());
        atualizaSituacaoPagSeguro(pagSeguroService.buscaReferenciaIdTransacao(transactionId, configuracao), configuracao);
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public String buscaURLAutenticacaoCalendar() throws IOException {
        return googleService.getURLAutorizacaoCalendar(sessaoBean.getChaveEmpresa(),
                ResourceBundleUtil._default().getPropriedade("OAUTH_CALENDAR_REDIRECT_URL"));
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public String buscaURLAutenticacaoYouTube() throws IOException {
        return googleService.getURLAutorizacaoYouTube(sessaoBean.getChaveEmpresa(),
                ResourceBundleUtil._default().getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"));
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public ConfiguracaoCalendarEmpresaDTO atualiza(ConfiguracaoCalendarEmpresaDTO configuracao) {
        paramService.salvaConfiguracaoCalendar(configuracao, sessaoBean.getChaveEmpresa());
        return paramService.buscaConfiguracaoCalendar(sessaoBean.getChaveEmpresa());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public List<CalendarioGoogleDTO> buscaVisoesCalendar() throws IOException {
        return googleService.buscaCalendarios(sessaoBean.getChaveEmpresa());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public ConfiguracaoYouTubeEmpresaDTO atualiza(ConfiguracaoYouTubeEmpresaDTO configuracao) {
        paramService.salvaConfiguracaoYouTube(configuracao, sessaoBean.getChaveEmpresa());
        return paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveEmpresa());
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public void iniciaConfiguracaoCalendar(String code) {
        try {
            googleService.saveCredentialsGoogleCalendar(sessaoBean.getChaveEmpresa(),
                    ResourceBundleUtil._default().getPropriedade("OAUTH_CALENDAR_REDIRECT_URL"), code);

            ConfiguracaoCalendarEmpresaDTO config = paramService.buscaConfiguracaoCalendar(sessaoBean.getChaveEmpresa());
            config.setIdCalendario(googleService.buscaIdsCalendar(sessaoBean.getChaveEmpresa()));
            paramService.salvaConfiguracaoCalendar(config, sessaoBean.getChaveEmpresa());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new ServiceException("mensagens.MSG-049");
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public void iniciaConfiguracaoYouTube(String code) {
        try {
            googleService.saveCredentialsYouTube(sessaoBean.getChaveEmpresa(),
                    ResourceBundleUtil._default().getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), code);
            
            ConfiguracaoYouTubeEmpresaDTO config = paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveEmpresa());
            config.setIdCanal(googleService.buscaIdCanalYouTube(sessaoBean.getChaveEmpresa()));
            paramService.salvaConfiguracaoYouTube(config, sessaoBean.getChaveEmpresa());
            
            Institucional institucional = recuperaInstitucional();
            if (!institucional.getRedesSociais().containsKey("youtube")){
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
        paramService.set(sessaoBean.getChaveEmpresa(),
                TipoParametro.GOOGLE_CALENDAR_ID, null);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public void desvinculaYouTube() {
        paramService.set(sessaoBean.getChaveEmpresa(),
                TipoParametro.YOUTUBE_CHANNEL_ID, null);
    }

    @Override
    public BuscaPaginadaEventosCalendarioDTO buscaEventos(String pagina, Integer total) {try {
        return googleService.buscaEventosCalendar(sessaoBean.getChaveEmpresa(), pagina, total);
    } catch (IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
        return new BuscaPaginadaEventosCalendarioDTO(Collections.EMPTY_LIST, null);
    }
    }

    @Override
    public List<VideoDTO> buscaVideos() {
        try {
            return googleService.buscaVideosYouTube(sessaoBean.getChaveEmpresa());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_GOOGLE_CALENDAR)
    public ConfiguracaoCalendarEmpresaDTO buscaConfiguracaoCalendar() {
        return paramService.buscaConfiguracaoCalendar(sessaoBean.getChaveEmpresa());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_YOUTUBE)
    public ConfiguracaoYouTubeEmpresaDTO buscaConfiguracaoYouTube() {
        return paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveEmpresa());
    }

    @Override
    @AllowColaborador(Funcionalidade.ANIVERSARIANTES)
    public List<Colaborador> buscaProximosAniversariantes() {
        Empresa empresa = daoService.find(Empresa.class, sessaoBean.getChaveEmpresa());

        TimeZone timeZone = TimeZone.getTimeZone(empresa.getTimezone());
        Calendar dateCal = Calendar.getInstance(timeZone);
        dateCal.setTime(new Date());

        int mes = dateCal.get(Calendar.MONTH) + 1;

        int inicio = mes * 100 + 1;

        dateCal.add(Calendar.DAY_OF_MONTH, 30);

        int fim = mes * 100 + DateUtil.getDiasMes(mes, dateCal.get(Calendar.YEAR));

        return daoService.findWith(QueryAdmin.PROXIMOS_ANIVERSARIANTES.create(empresa.getChave(), inicio, fim));
    }

    @Override
    public List<CategoriaAudio> buscaCategoriasAudio() {
        if (sessaoBean.isAdmin()) {
            return daoService.findWith(QueryAdmin.CATEGORIA_AUDIO.create(sessaoBean.getChaveEmpresa()));
        } else {
            return daoService.findWith(QueryAdmin.CATEGORIA_USADAS_AUDIO.create(sessaoBean.getChaveEmpresa()));
        }
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public CategoriaAudio cadastra(CategoriaAudio categoria) {
        categoria.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));
        return daoService.create(categoria);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public Audio cadastra(Audio audio) throws InvalidDataException, IOException, UnsupportedTagException {
        audio.setEmpresa(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()));

        audio.setTipo(TipoAudio.LOCAL);

        audio.setCategoria(daoService.find(CategoriaAudio.class, new RegistroEmpresaId(
                sessaoBean.getChaveEmpresa(), audio.getCategoria().getId()
        )));

        audio.setAudio(arquivoService.buscaArquivo(audio.getAudio().getId()));

        arquivoService.registraUso(audio.getAudio().getId());

        if (audio.getCapa() != null) {
            audio.setCapa(arquivoService.buscaArquivo(audio.getCapa().getId()));

            arquivoService.registraUso(audio.getCapa().getId());
        }

        File file = EntityFileManager.get(audio.getAudio(), "dados");

        audio.setTamamnhoArquivo(file.length());

        Mp3File mp3 = new Mp3File(file);

        audio.setTempoAudio(mp3.getLengthInSeconds());

        return daoService.create(audio);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public Audio atualiza(Audio audio) throws InvalidDataException, IOException, UnsupportedTagException {
        if (!TipoAudio.LOCAL.equals(audio.getTipo())) {
            throw new ServiceException("mensagens.MSG-403");
        }

        audio.setCategoria(daoService.find(CategoriaAudio.class, new RegistroEmpresaId(
                sessaoBean.getChaveEmpresa(), audio.getCategoria().getId()
        )));

        Audio entidade = daoService.find(Audio.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), audio.getId()));

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

            Mp3File mp3 = new Mp3File(file);

            audio.setTempoAudio(mp3.getLengthInSeconds());
        }


        return daoService.update(audio);
    }

    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public void removeAudio(Long audio) {
        Audio entidade = daoService.find(Audio.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), audio));

        arquivoService.registraDesuso(entidade.getAudio().getId());

        daoService.delete(Audio.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), audio));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AUDIOS)
    public Audio buscaAudio(Long audio) {
        return daoService.find(Audio.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), audio));
    }

    @Override
    public BuscaPaginadaDTO<Audio> buscaTodos(FiltroAudioDTO filtro) {
        return daoService.findWith(new FiltroAudio(sessaoBean.getChaveEmpresa(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<GaleriaDTO> buscaGaleriasFotos(Integer pagina) {
        return flickrService.buscaGaleriaFotos(sessaoBean.getChaveEmpresa(), pagina);
    }

    @Override
    public BuscaPaginadaDTO<FotoDTO> buscaFotos(FiltroFotoDTO filtro) {
        return flickrService.buscaFotos(sessaoBean.getChaveEmpresa(), filtro);
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public ConfiguracaoFlickrEmpresaDTO buscaConfiguracaoFlickr() {
        return paramService.buscaConfiguracaoFlickr(sessaoBean.getChaveEmpresa());
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public String buscaURLAutenticacaoFlickr() throws IOException {
        return flickrService.buscaURLAutenticacaoFlickr(sessaoBean.getChaveEmpresa(),
                MessageFormat.format(ResourceBundleUtil._default()
                        .getPropriedade("OAUTH_FLICKR_REDIRECT_URL"), sessaoBean.getChaveEmpresa()));
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public void iniciaConfiguracaoFlickr(String token, String verifier) {
        flickrService.iniciaConfiguracaoFlickr(sessaoBean.getChaveEmpresa(), token, verifier);
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR_FLICKR)
    public void desvinculaFlickr() {
        flickrService.devinculaFlickr(sessaoBean.getChaveEmpresa());
    }

    @Schedule(hour = "*", minute = "0/15")
    public void verificaPagSeguro() {
        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS.create());
        for (Empresa empresa : empresas) {
            ConfiguracaoEmpresaDTO configuracao = paramService.buscaConfiguracao(empresa.getChave());
            if (configuracao != null && configuracao.isPagSeguroConfigurado()){
                List<String> referencias = daoService.findWith(QueryAdmin.REFERENCIAS_INSCRICOES_PENDENTES.create(empresa.getChave()));
                
                for (String referencia : referencias){
                    atualizaSituacaoPagSeguro(referencia, configuracao);
                }
            }
        }
    }

    public void atualizaSituacaoPagSeguro(String referencia, ConfiguracaoEmpresaDTO configuracao){
        switch (pagSeguroService.getStatusPagamento(referencia, configuracao)){
            case PAGO:
            {
                List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.INSCRICOES_POR_REFERENCIA.create(referencia));
                
                if (!inscricoes.isEmpty()){
                    Colaborador colaborador = inscricoes.get(0).getColaborador();
                    Evento evento = inscricoes.get(0).getEvento();
                    Institucional institucional = daoService.find(Institucional.class, evento.getEmpresa().getChave());
                    BigDecimal total = BigDecimal.ZERO;
                    
                    for (InscricaoEvento inscricao : inscricoes){
                        inscricao.confirmada();
                        daoService.update(inscricao);
                        total = total.add(inscricao.getValor());
                    }
                    
                    Locale locale = Locale.forLanguageTag(evento.getEmpresa().getLocale());
                    NumberFormat nformat = NumberFormat.getCurrencyInstance(locale);
                    
                    String subject = MensagemUtil.getMensagem("email.confirmacao_inscricao.subject", evento.getEmpresa().getLocale());
                    String title = MensagemUtil.getMensagem("email.confirmacao_inscricao.message.title", evento.getEmpresa().getLocale(), colaborador.getNome());
                    String text = MensagemUtil.getMensagem("email.confirmacao_inscricao.message.text", evento.getEmpresa().getLocale(),
                            evento.getNome(), nformat.format(total), institucional.getEmail());
                    
                    notificacaoService.sendNow(
                            MensagemUtil.email(institucional, subject,
                                    new CalvinEmailDTO(null, Arrays.asList(new CalvinEmailDTO.Materia(title, text)))),
                            new FiltroEmailDTO(evento.getEmpresa(), colaborador.getId()));
                    
                    
                    scheduleRelatoriosInscritos(evento);
                }
                
            }
            break;
            case CANCELADO:
            {
                List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.INSCRICOES_POR_REFERENCIA.create(referencia));
                for (InscricaoEvento inscricao : inscricoes){
                    inscricao.cancelada();
                    daoService.update(inscricao);
                    scheduleRelatoriosInscritos(inscricao.getEvento());
                }
                break;
            }
        }
    }
    
    @Schedule(hour = "*")
    public void enviaMensagensDia() {
        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS.create());
        for (Empresa empresa : empresas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer hora = cal.get(Calendar.HOUR_OF_DAY);
            
            MensagemDia atual = daoService.findWith(QueryAdmin.MENSAGEM_DIAS_POR_STATUS.createSingle(empresa.getId(), StatusMensagemDia.ATIVO));
            if (atual == null || !DateUtil.equalsSemHoras(atual.getUltimoEnvio(), new Date())){
                if (atual != null){
                    atual.habilitado();
                    atual = daoService.update(atual);
                }
                
                MensagemDia mensagemDia = daoService.findWith(QueryAdmin.SORTEIA_MENSAGEM_DIA.createSingle(empresa.getId()));
                if (mensagemDia != null){
                    mensagemDia.ativo();
                    atual = daoService.update(mensagemDia);
                }
            }
            
            String titulo = paramService.get(empresa.getChave(), TipoParametro.TITULO_MENSAGEM_DIA);
            if (StringUtil.isEmpty(titulo)){
                titulo = MensagemUtil.getMensagem("push.mensagem_dia.title", empresa.getLocale());
            }
            
            if (atual != null && atual.isAtivo()){
                for (HorasEnvioNotificacao hev : HorasEnvioNotificacao.values()){
                    if (hev.getHoraInt().equals(hora)){
                        enviaPush(new FiltroDispositivoNotificacaoDTO(empresa, hev),
                                titulo, atual.getMensagem(), TipoNotificacao.MENSAGEM_DIA, true);
                        break;
                    }
                }
            }
        }
    }
    
    @Schedule(hour = "*")
    public void enviaParabensAniversario() {
        LOGGER.info("Iniciando envio de notificações de aniversário.");

        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS.create());

        LOGGER.info(empresas.size() + " empresas encontradas para envio de notificações de aniversário.");

        for (Empresa empresa : empresas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer hora = cal.get(Calendar.HOUR_OF_DAY);
            
            if (hora.equals(12)){
                LOGGER.info("Prepara envio de notificações de aniversário para " + empresa.getChave());

                List<Colaborador> aniversariantes = daoService.findWith(QueryAdmin.ANIVERSARIANTES.create(empresa.getChave()));
                
                for (Colaborador colaborador : aniversariantes){
                    String titulo = paramService.get(empresa.getChave(), TipoParametro.TITULO_ANIVERSARIO);
                    if (StringUtil.isEmpty(titulo)){
                        titulo = MensagemUtil.getMensagem("push.aniversario.title", empresa.getLocale());
                    }
                    
                    String texto = paramService.get(empresa.getChave(), TipoParametro.TEXTO_ANIVERSARIO);
                    if (StringUtil.isEmpty(texto)){
                        texto = MensagemUtil.getMensagem("push.aniversario.message",
                                empresa.getLocale(), colaborador.getNome(), empresa.getNome());
                    }else{
                        texto = MessageFormat.format(texto, colaborador.getNome(), empresa.getNome());
                    }
                    
                    enviaPush(new FiltroDispositivoNotificacaoDTO(empresa, colaborador.getId()), titulo, texto, TipoNotificacao.ANIVERSARIO, false);
                }
            } else {
                LOGGER.info(empresa.getChave() + " fora do horário para envio de notificações de aniversário");
            }
        }
    }

    @Schedule(hour = "*")
    public void enviaNotificacoesPublicacoes() {
        LOGGER.info("Iniciando envio de notificações de publicações.");

        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS_COM_PUBLICACOES_A_DIVULGAR.create());

        LOGGER.info(empresas.size() + " empresas encontradas para envio de notificações de publicações.");

        for (Empresa empresa : empresas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de publicações para " + empresa.getChave());

                String titulo = paramService.get(empresa.getChave(), TipoParametro.TITULO_PUBLICACAO);
                if (StringUtil.isEmpty(titulo)){
                    titulo = MensagemUtil.getMensagem("push.publicacao.title", empresa.getLocale());
                }

                String texto = paramService.get(empresa.getChave(), TipoParametro.TEXTO_PUBLICACAO);
                if (StringUtil.isEmpty(texto)){
                    texto = MensagemUtil.getMensagem("push.publicacao.message", empresa.getLocale(), empresa.getNome());
                }

                enviaPush(new FiltroDispositivoNotificacaoDTO(empresa), titulo, texto, TipoNotificacao.PUBLICACAO, false);

                daoService.execute(QueryAdmin.UPDATE_PUBLICACOES_NAO_DIVULGADOS.create(empresa.getChave()));
            } else {
                LOGGER.info("Hora fora do limite de envio de notificações de publicações para " + empresa.getChave());
            }
        }
    }

    @Schedule(hour = "*")
    public void enviaNotificacoesBoletins() {
        LOGGER.info("Iniciando envio de notificações de boletins.");

        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS_COM_BOLETINS_A_DIVULGAR.create());

        LOGGER.info(empresas.size() + " empresas encontradas para envio de notificações de boletins.");

        for (Empresa empresa : empresas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de boletins para " + empresa.getChave());

                String titulo = paramService.get(empresa.getChave(), TipoParametro.TITULO_BOLETIM);
                if (StringUtil.isEmpty(titulo)){
                    titulo = MensagemUtil.getMensagem("push.boletim.title", empresa.getLocale());
                }

                String texto = paramService.get(empresa.getChave(), TipoParametro.TEXTO_BOLETIM);
                if (StringUtil.isEmpty(texto)){
                    texto = MensagemUtil.getMensagem("push.boletim.message", empresa.getLocale(), empresa.getNome());
                }

                enviaPush(new FiltroDispositivoNotificacaoDTO(empresa), titulo, texto, TipoNotificacao.BOLETIM, false);

                daoService.execute(QueryAdmin.UPDATE_BOLETINS_NAO_DIVULGADOS.create(empresa.getChave()));
            } else {
                LOGGER.info("Hora fora do limite de envio de notificações de boletins para " + empresa.getChave());
            }
        }
    }
    
    @Schedule(hour = "*")
    public void enviaNotificacoesDocumentos() {
        LOGGER.info("Iniciando envio de notificações de documentos.");

        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS_COM_DOCUMENTOS_A_DIVULGAR.create());

        LOGGER.info(empresas.size() +" empresas encontrada para notificação de documentos.");

        for (Empresa empresa : empresas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de documento para " + empresa.getChave());

                String titulo = paramService.get(empresa.getChave(), TipoParametro.TITULO_DOCUMENTO);
                if (StringUtil.isEmpty(titulo)){
                    titulo = MensagemUtil.getMensagem("push.documento.title", empresa.getLocale());
                }

                String texto = paramService.get(empresa.getChave(), TipoParametro.TEXTO_DOCUMENTO);
                if (StringUtil.isEmpty(texto)){
                    texto = MensagemUtil.getMensagem("push.documento.message", empresa.getLocale(), empresa.getNome());
                }

                enviaPush(new FiltroDispositivoNotificacaoDTO(empresa), titulo, texto, TipoNotificacao.DOCUMENTO, false);

                daoService.execute(QueryAdmin.UPDATE_DOCUMENTOS_NAO_DIVULGADOS.create(empresa.getChave()));
            } else {
                LOGGER.info(empresa.getChave() + " fora do horário para envio de notiicações de documentos.");
            }
        }
    }

    @Schedule(hour = "*")
    public void enviaNotificacoesNoticias() {
        LOGGER.info("Iniciando envio de notificações de notícia.");

        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS_COM_NOTICIAS_A_DIVULGAR.create(TipoNoticia.NOTICIA));

        LOGGER.info(empresas.size() +" empresas encontrada para notificação de notícias.");

        for (Empresa empresa : empresas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de notícia para " + empresa.getChave());

                String titulo = paramService.get(empresa.getChave(), TipoParametro.TITULO_NOTICIA);
                if (StringUtil.isEmpty(titulo)){
                    titulo = MensagemUtil.getMensagem("push.noticia.title", empresa.getLocale());
                }

                String texto = paramService.get(empresa.getChave(), TipoParametro.TEXTO_NOTICIA);
                if (StringUtil.isEmpty(texto)){
                    texto = MensagemUtil.getMensagem("push.noticia.message", empresa.getLocale(), empresa.getNome());
                }

                enviaPush(new FiltroDispositivoNotificacaoDTO(empresa), titulo, texto, TipoNotificacao.NOTICIA, false);

                daoService.execute(QueryAdmin.UPDATE_NOTICIAS_NAO_DIVULGADAS.create(empresa.getChave(), TipoNoticia.NOTICIA));
            } else {
                LOGGER.info(empresa.getChave() + " fora do horário para envio de notiicações de notícias.");
            }
        }
    }

    @Schedule(hour = "*")
    public void enviaNotificacoesClassificados() {
        LOGGER.info("Iniciando envio de notificações de classificados.");

        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS_COM_NOTICIAS_A_DIVULGAR.create(TipoNoticia.CLASSIFICADOS));

        LOGGER.info(empresas.size() +" empresas encontrada para notificação de notícias.");

        for (Empresa empresa : empresas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);

            if (horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO) {
                LOGGER.info("Preparando envio de notificações de notícia para " + empresa.getChave());

                String titulo = paramService.get(empresa.getChave(), TipoParametro.TITULO_CLASSIFICADOS);
                if (StringUtil.isEmpty(titulo)){
                    titulo = MensagemUtil.getMensagem("push.classificados.title", empresa.getLocale());
                }

                String texto = paramService.get(empresa.getChave(), TipoParametro.TEXTO_CLASSIFICADOS);
                if (StringUtil.isEmpty(texto)){
                    texto = MensagemUtil.getMensagem("push.classificados.message", empresa.getLocale(), empresa.getNome());
                }

                enviaPush(new FiltroDispositivoNotificacaoDTO(empresa), titulo, texto, TipoNotificacao.CLASSIFICADOS, false);

                daoService.execute(QueryAdmin.UPDATE_NOTICIAS_NAO_DIVULGADAS.create(empresa.getChave(), TipoNoticia.CLASSIFICADOS));
            } else {
                LOGGER.info(empresa.getChave() + " fora do horário para envio de notiicações de notícias.");
            }
        }
    }

    @Schedule(hour = "*", minute = "*/5")
    public void enviaNotificacoesYouTubeAoVivo() {
        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS.create());
        for (Empresa empresa : empresas) {
            ConfiguracaoYouTubeEmpresaDTO config = paramService.buscaConfiguracaoYouTube(empresa.getChave());
            if (config.isConfigurado()){
                try{
                    List<VideoDTO> streamings = googleService.buscaStreamsAtivosYouTube(empresa.getChave());
                    
                    for (VideoDTO video : streamings){
                        if (!Persister.file(NotificacaoYouTubeAoVivo.class, video.getId()).exists()){
                            Persister.save(new NotificacaoYouTubeAoVivo(video), video.getId());
                            
                            try{
                                String titulo = config.getTituloAoVivo();
                                if (StringUtil.isEmpty(titulo)){
                                    titulo = MensagemUtil.getMensagem("push.youtube.aovivo.title", empresa.getLocale());
                                }
                                
                                String texto = config.getTextoAoVivo();
                                if (StringUtil.isEmpty(texto)){
                                    texto = MensagemUtil.getMensagem("push.youtube.aovivo.message", empresa.getLocale(), video.getTitulo());
                                }
                                
                                enviaPush(new FiltroDispositivoNotificacaoDTO(empresa, true), titulo, texto, TipoNotificacao.YOUTUBE, false);
                            }catch(Exception e){
                                Persister.remove(NotificacaoYouTubeAgendado.class, video.getId());
                                throw e;
                            }
                        }
                    }
                }catch(Exception e){
                    Logger.getLogger(AppServiceImpl.class.getName()).log(Level.SEVERE, "Erro ao verificar vídeos ao vivo para " + empresa.getChave(), e);
                }
            }
        }
    }
    
    @Schedule(hour = "*")
    public void enviaNotificacoesYouTubeAgendados() {
        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS.create());
        for (Empresa empresa : empresas) {
            ConfiguracaoYouTubeEmpresaDTO config = paramService.buscaConfiguracaoYouTube(empresa.getChave());
            
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(empresa.getTimezone()));
            Integer horaAtual = cal.get(Calendar.HOUR_OF_DAY);
            
            if (config.isConfigurado()){
                try{
                    List<VideoDTO> streamings = googleService.buscaStreamsAgendadosYouTube(empresa.getChave());
                    
                    for (VideoDTO video : streamings){
                        if (!Persister.file(NotificacaoYouTubeAgendado.class, video.getId()).exists() &&
                                DateUtil.equalsSemHoras(DateUtil.getDataAtual(), video.getAgendamento()) &&
                                // Verifica se está em horário útil para fazer a notificação
                                horaAtual >= HORA_MINIMA_NOTIFICACAO && horaAtual <= HORA_MAXIMA_NOTIFICACAO){
                            
                            Persister.save(new NotificacaoYouTubeAgendado(video), video.getId());
                            
                            try{
                                String horario = MensagemUtil.formataHora(video.getAgendamento(), empresa.getLocale(), empresa.getTimezone());

                                String titulo = config.getTituloAoVivo();
                                if (StringUtil.isEmpty(titulo)){
                                    titulo = MensagemUtil.getMensagem("push.youtube.agendado.title",
                                            empresa.getLocale(), video.getTitulo(), horario);
                                }
                                
                                String texto = config.getTextoAoVivo();
                                if (StringUtil.isEmpty(texto)){
                                    texto = MensagemUtil.getMensagem("push.youtube.agendado.message", empresa.getLocale(),
                                            video.getTitulo(), horario);
                                }
                                
                                enviaPush(new FiltroDispositivoNotificacaoDTO(empresa, true), titulo, texto, TipoNotificacao.YOUTUBE, false);
                            }catch(Exception e){
                                Persister.remove(NotificacaoYouTubeAgendado.class, video.getId());
                                throw e;
                            }
                        }
                    }
                }catch(Exception e){
                    Logger.getLogger(AppServiceImpl.class.getName()).log(Level.SEVERE, "Erro ao verificar vídeos agendados para " + empresa.getChave(), e);
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
