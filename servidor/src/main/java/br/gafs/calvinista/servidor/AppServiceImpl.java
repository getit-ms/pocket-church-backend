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
import br.gafs.calvinista.servidor.google.GoogleService;
import br.gafs.calvinista.servidor.pagseguro.PagSeguroService;
import br.gafs.calvinista.util.MensagemUtil;
import br.gafs.calvinista.util.PDFToImageConverterUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.dao.QueryParameters;
import br.gafs.exceptions.ServiceException;
import br.gafs.file.EntityFileManager;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import br.gafs.util.image.ImageUtil;
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
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
    
    @EJB
    private DAOService daoService;
    
    @Inject
    private SessaoBean sessaoBean;
    
    @EJB
    private ArquivoService arquivoService;
    
    @EJB
    private MensagemService notificacaoService;
    
    @EJB
    private PagSeguroService pagSeguroService;
    
    @EJB
    private GoogleService googleService;
    
    @EJB
    private ParametroService  paramService;
    
    @EJB
    private ProcessamentoService processamentoService;
    
    @Override
    @AllowAdmin
    public StatusAdminDTO buscaStatus(){
        StatusAdminDTO status = new StatusAdminDTO();
        status.setVersiculoDiario(buscaVersiculoDiario());
        
        if (sessaoBean.temPermissao(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)){
            Number pedidos = daoService.findWith(new FiltroPedidoOracao(null, sessaoBean.getChaveIgreja(),
                    new FiltroPedidoOracaoDTO(null, null, Arrays.asList(StatusPedidoOracao.PENDENTE), 1, 10)).getCountQuery());
            
            if (pedidos.intValue() > 0){
                status.addNotificacao("mensagens.MSG-036",
                        new QueryParameters("quantidade", pedidos));
            }
        }

        if (sessaoBean.temPermissao(Funcionalidade.MANTER_EVENTOS)){
            // TODO verificar a quantidade de inscrições pendentes em eventos
        }

        if (sessaoBean.temPermissao(Funcionalidade.MANTER_EBD)){
            // TODO verificar a quantidade de inscrições pendentes em cursos EBD
        }

        return status;
    }
    
    @Override
    public BuscaPaginadaDTO<NotificationSchedule> buscaNotificacoes(FiltroNotificacoesDTO filtro) {
        BuscaPaginadaDTO<NotificationSchedule> busca = daoService.findWith(new FiltroNotificacoes(sessaoBean.getChaveIgreja(),
                sessaoBean.getChaveDispositivo(), sessaoBean.getIdMembro(), filtro));
        marcaNotificacoesComoLidas();
        return busca;
    }
    
    @Override
    public Long countNotificacoesNaoLidas() {
        return daoService.findWith(QueryNotificacao.COUNT_NOTIFICACOES_NAO_LIDAS.
                createSingle(sessaoBean.getChaveIgreja(), sessaoBean.getChaveDispositivo(),
                        sessaoBean.getIdMembro() == null ? 0 : sessaoBean.getIdMembro()));
    }
    
    @Override
    public void clearNotificacoes(){
        daoService.execute(QueryNotificacao.CLEAR_NOTIFICACOES.
                create(sessaoBean.getChaveIgreja(), sessaoBean.getChaveDispositivo(),
                        sessaoBean.getIdMembro() == null ? 0 : sessaoBean.getIdMembro()));
    }
    
    @Override
    public void removeNotificacao(Long notificacao){
        daoService.execute(QueryNotificacao.REMOVE_NOTIFICACAO.
                create(sessaoBean.getChaveIgreja(), sessaoBean.getChaveDispositivo(),
                        sessaoBean.getIdMembro() == null ? 0 : sessaoBean.getIdMembro(), notificacao));
    }

    public void marcaNotificacoesComoLidas() {
        daoService.execute(QueryNotificacao.MARCA_NOTIFICACOES_COMO_LIDAS.
                create(sessaoBean.getChaveIgreja(), sessaoBean.getChaveDispositivo(),
                        sessaoBean.getIdMembro() == null ? 0 : sessaoBean.getIdMembro()));
    }
    
    @Override
    @AllowAdmin
    @AllowMembro
    public List<ReleaseNotes> buscaReleaseNotes(TipoVersao tipo) {
        return daoService.findWith(QueryAdmin.RELEASE_NOTES.create(tipo));
    }
    
    private VersiculoDiario buscaVersiculoDiario(){
        return daoService.findWith(QueryAdmin.VERSICULOS_POR_STATUS.
                createSingle(sessaoBean.getChaveIgreja(), StatusVersiculoDiario.ATIVO));
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public List<Membro> buscaPastores() {
        return daoService.findWith(QueryAdmin.PASTORES_ATIVOS.
                create(sessaoBean.getChaveIgreja()));
    }
    
    @Audit
    @Override
    public Chamado solicita(Chamado chamado) {
        if (sessaoBean.isAdmin()){
            chamado.setTipo(TipoChamado.SUPORTE);
            
            if (sessaoBean.getIdMembro() == null ||
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
                        chamado.getDescricao(), chamado.getIgrejaSolicitante().getNome(),
                        chamado.getNomeSolicitante(), chamado.getEmailSolicitante(),
                        chamado.getDispositivoSolicitante().getUuid(),
                        chamado.getDispositivoSolicitante().getVersao()),
                MessageFormat.format(ResourceBundleUtil._default().getPropriedade("CHAMADO_SUBJECT"),
                        chamado.getIgrejaSolicitante().getChave().toUpperCase(),
                        chamado.getCodigo(), chamado.getTipo().name()),
                ResourceBundleUtil._default().getPropriedade("CHAMADO_MAIL").split("\\s*,\\s*"));
        
        return chamado;
    }
    
    @Override
    @AllowAdmin(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)
    public Chamado buscaChamado(Long id) {
        Chamado chamado = daoService.find(Chamado.class, id);
        if (chamado == null || !chamado.getIgrejaSolicitante().getChave().equals(sessaoBean.getChaveIgreja()) ||
                (chamado.isSuporte() && (sessaoBean.getIdMembro() == null ||
                !sessaoBean.temPermissao(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)))){
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
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Membro cadastra(Membro membro) {
        membro.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
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
        
        boolean gerarSenha = entidade.isSenhaUndefined();
        
        entidade.membro();
        entidade = daoService.update(entidade);
        
        if (gerarSenha){
            String senha = SenhaUtil.geraSenha(8);
            
            entidade.setSenha(SenhaUtil.encryptSHA256(senha));
            
            entidade = daoService.update(entidade);
            
            String subject = MensagemUtil.getMensagem("email.dar_acesso.subject", entidade.getIgreja().getLocale());
            String title = MensagemUtil.getMensagem("email.dar_acesso.message.title", entidade.getIgreja().getLocale(),
                    entidade.getNome());
            String text = MensagemUtil.getMensagem("email.dar_acesso.message.text", entidade.getIgreja().getLocale(),
                    entidade.getIgreja().getNome());
            
            notificacaoService.sendNow(
                    MensagemUtil.email(recuperaInstitucional(), subject,
                            new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, "", senha), Collections.EMPTY_LIST)),
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
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Acesso buscaAcessoAdmin(Long membro) {
        return daoService.find(Acesso.class, new AcessoId(new RegistroIgrejaId(
                sessaoBean.getChaveIgreja(), membro)));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public void removeMembro(Long membro) {
        Membro entidade = buscaMembro(membro);
        
        if (!entidade.isMembro()){
            entidade.exclui();
            daoService.update(entidade);
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
        return daoService.findWith(new FiltroLivroBiblia(sessaoBean.getChaveIgreja(), filtro));
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
            if (!entidade.isMembro()){
                entidade = darAcessoMembro(membro);
            }
            
            acesso = daoService.create(new Acesso(entidade));
        }
        
        acesso.setPerfis(perfis);
        acesso.setMinisterios(ministerios);
        
        if (!acesso.possuiPermissao(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)){
            if (entidade.getId().equals(sessaoBean.getIdMembro())) {
                throw new ServiceException("mensagens.MSG-015");
            }
        }
        
        return daoService.update(acesso);
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
        return daoService.update(membro);
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    @AllowMembro(Funcionalidade.CONSULTAR_CONTATOS_IGREJA)
    public Membro buscaMembro(Long membro) {
        return daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), membro));
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    @AllowMembro(Funcionalidade.CONSULTAR_CONTATOS_IGREJA)
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
        daoService.delete(Perfil.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), perfil));
    }
    
    @Override
    @AllowAdmin({
        Funcionalidade.MANTER_PERFIS,
        Funcionalidade.MANTER_MEMBROS
    })
    public List<Perfil> buscaPerfis() {
        return daoService.findWith(QueryAdmin.PERFIS.create(sessaoBean.getChaveIgreja()));
    }
    
    @Schedule(hour = "*", minute = "*/5")
    public void processaBoletins() {
        if (Boletim.locked() < 5){
            List<Boletim> boletins = daoService.findWith(QueryAdmin.BOLETINS_PROCESSANDO.create());
            for (Boletim boletim : boletins){
                try{
                    if (!Boletim.locked(new RegistroIgrejaId(boletim.getChaveIgreja(), boletim.getId()))){
                        processamentoService.processa(new ProcessamentoBoletim(boletim));
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public Boletim cadastra(Boletim boletim) throws IOException {
        boletim.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        boletim.setBoletim(arquivoService.buscaArquivo(boletim.getBoletim().getId()));
        if (trataTrocaPDF(boletim)){
            boletim.processando();
        }
        return daoService.create(boletim);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_CIFRAS)
    public Cifra cadastra(Cifra cifra) throws IOException {
        cifra.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        cifra.setCifra(arquivoService.buscaArquivo(cifra.getCifra().getId()));
        if (trataTrocaPDF(cifra)){
            trataPaginasPDF(cifra);
        }
        return daoService.create(cifra);
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
    public String extraiTexto(Long idArquivo){
        try {
            File file = EntityFileManager.get(arquivoService.buscaArquivo(idArquivo), "dados");
            
            if (!file.exists()){
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
    
    private int trataPaginasPDF(ArquivoPDF pdf) throws IOException {
        return trataPaginasPDF(pdf, -1);
    }
    
    private int trataPaginasPDF(final ArquivoPDF pdf, final int limitePaginas) throws IOException {
        final int offset = pdf.getPaginas().size();
        
        return PDFToImageConverterUtil.convert(EntityFileManager.
                get(pdf.getPDF(), "dados")).forEachPage(new PDFToImageConverterUtil.PageHandler() {
                    @Override
                    public void handle(int page, byte[] dados) throws IOException {
                        if (page < offset || (limitePaginas > 0 && page >= (offset + limitePaginas))){
                            return;
                        }
                        
                        if (page == 0){
                            pdf.setThumbnail(arquivoService.cadastra(pdf.getIgreja(), pdf.getPDF().getNome().
                                    replaceFirst(".[pP][dD][fF]$", "") + "_thumbnail.png", ImageUtil.redimensionaImagem(dados, 500, 500)));
                            arquivoService.registraUso(pdf.getIgreja().getChave(), pdf.getThumbnail().getId());
                            pdf.getThumbnail().clearDados();
                        }
                        
                        Arquivo pagina = arquivoService.cadastra(pdf.getIgreja(), pdf.getPDF().getNome().
                                replaceFirst(".[pP][dD][fF]$", "") + "_page"
                                + new DecimalFormat("00000").format(page + 1) + ".png", dados);
                        pdf.getPaginas().add(pagina);
                        arquivoService.registraUso(pdf.getIgreja().getChave(), pagina.getId());
                        pagina.clearDados();
                    }
                });
    }
    
    private boolean trataTrocaPDF(final ArquivoPDF pdf) {
        if (!pdf.getPDF().isUsed()) {
            if (pdf.getId() != null){
                ArquivoPDF old = daoService.find(pdf.getClass(), new RegistroIgrejaId(sessaoBean.getChaveIgreja(), pdf.getId()));
                arquivoService.registraDesuso(old.getPDF().getId());
                arquivoService.registraDesuso(old.getThumbnail().getId());
                
                List<Arquivo> pages = new ArrayList<Arquivo>(old.getPaginas());
                old.getPaginas().clear();
                for (Arquivo arq : pages) {
                    arquivoService.registraDesuso(arq.getId());
                }
            }
            
            arquivoService.registraUso(pdf.getPDF().getId());
            
            return true;
        }
        
        return false;
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public Boletim atualiza(Boletim boletim) throws IOException {
        boletim.setBoletim(arquivoService.buscaArquivo(boletim.getBoletim().getId()));
        boletim.setUltimaAlteracao(DateUtil.getDataAtual());
        if (trataTrocaPDF(boletim)){
            boletim.processando();
        }
        return daoService.update(boletim);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_CIFRAS)
    public Cifra atualiza(Cifra cifra) throws IOException {
        cifra.setCifra(arquivoService.buscaArquivo(cifra.getCifra().getId()));
        if (trataTrocaPDF(cifra)){
            trataPaginasPDF(cifra);
        }
        return daoService.update(cifra);
    }
    
    @Override
    public Boletim buscaBoletim(Long boletim) {
        return daoService.find(Boletim.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), boletim));
    }
    
    @Override
    public Cifra buscaCifra(Long cifra) {
        return daoService.find(Cifra.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), cifra));
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public void removeBoletim(Long boletim) {
        Boletim entidade = buscaBoletim(boletim);
        
        if (entidade != null){
            for (Arquivo page : entidade.getPaginas()) {
                arquivoService.registraDesuso(page.getId());
            }
            
            arquivoService.registraDesuso(entidade.getBoletim().getId());
            
            if (entidade.getThumbnail() != null){
                arquivoService.registraDesuso(entidade.getThumbnail().getId());
            }
            
            daoService.delete(Boletim.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
        }
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_CIFRAS)
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
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public BuscaPaginadaDTO<Boletim> buscaTodos(FiltroBoletimDTO filtro) {
        return daoService.findWith(new FiltroBoletim(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }
    
    @Override
    public BuscaPaginadaDTO<Cifra> busca(FiltroCifraDTO filtro) {
        return daoService.findWith(new FiltroCifra(sessaoBean.getChaveIgreja(), filtro));
    }
    
    @Override
    public BuscaPaginadaDTO<Boletim> buscaPublicados(FiltroBoletimPublicadoDTO filtro) {
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
        Institucional institucional = daoService.find(Institucional.class, sessaoBean.getChaveIgreja());
        if (institucional == null) {
            institucional = new Institucional(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        }
        return institucional;
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public Estudo cadastra(Estudo estudo) {
        estudo.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        estudo.setMembro(buscaMembro(sessaoBean.getIdMembro()));
        return daoService.create(estudo);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public Estudo atualiza(Estudo estudo) {
        return daoService.update(estudo);
    }
    
    @Override
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
    public BuscaPaginadaDTO<Estudo> buscaTodos(FiltroEstudoDTO filtro) {
        return daoService.findWith(new FiltroEstudo(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }
    
    @Override
    public BuscaPaginadaDTO<Estudo> buscaPublicados(FiltroEstudoPublicadoDTO filtro) {
        return buscaTodos(filtro);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.ENVIAR_NOTIFICACOES)
    public void enviar(Notificacao notificacao) {
        notificacao.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));

        if (StringUtil.isEmpty(notificacao.getTitulo())){
            notificacao.setTitulo(MensagemUtil.getMensagem("push.notificacao.title",
                    notificacao.getIgreja().getLocale(), notificacao.getIgreja().getNomeAplicativo()));
        }
        
        notificacao = daoService.create(notificacao);
        
        FiltroDispositivoNotificacaoDTO filtro = new FiltroDispositivoNotificacaoDTO(notificacao.getIgreja());
        filtro.setApenasMembros(notificacao.isApenasMembros());
        for (Ministerio m : notificacao.getMinisteriosAlvo()){
            filtro.getMinisterios().add(m.getId());
        }
        
        enviaPush(filtro, notificacao.getTitulo(), notificacao.getMensagem(), TipoNotificacao.NOTIFICACAO, false);
    }
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public ResultadoVotacaoDTO buscaResultado(Long votacao) {
        Votacao entidade = buscaVotacao(votacao);
        
        ResultadoVotacaoDTO dto = new ResultadoVotacaoDTO(entidade);
        for (Questao questao : entidade.getQuestoes()) {
            ResultadoVotacaoDTO.ResultadoQuestaoDTO rq = dto.init(questao);
            
            for (Opcao o : questao.getOpcoes()) {
                List<Long> counts = daoService.findWith(QueryAdmin.RESULTADOS_OPCAO.create(o.getId()));
                for (Long count : counts) {
                    rq.resultado(o, count.intValue());
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
        
        if (entidade != null){
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
        return daoService.findWith(new FiltroVotacao(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), filtro));
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
                MensagemUtil.getMensagem("push.atendimento_pedido_oracao.title", entidade.getIgreja().getLocale()),
                MensagemUtil.getMensagem("push.atendimento_pedido_oracao.message", entidade.getIgreja().getLocale(),
                        MensagemUtil.formataDataHora(entidade.getDataSolicitacao(), entidade.getIgreja().getLocale(), entidade.getIgreja().getTimezone())),
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
        
        if (!sessaoBean.isAdmin()){
            enviaPush(new FiltroDispositivoNotificacaoDTO(atendimento.getIgreja(), atendimento.getCalendario().getPastor().getId()),
                    MensagemUtil.getMensagem("push.agendamento.title", atendimento.getIgreja().getLocale()),
                    MensagemUtil.getMensagem("push.agendamento.message", atendimento.getIgreja().getLocale(),
                            atendimento.getMembro().getNome(),
                            MensagemUtil.formataData(atendimento.getDataHoraInicio(),
                                    atendimento.getIgreja().getLocale(),
                                    atendimento.getIgreja().getTimezone()),
                            MensagemUtil.formataHora(atendimento.getDataHoraInicio(),
                                    atendimento.getIgreja().getLocale(),
                                    atendimento.getIgreja().getTimezone()),
                            MensagemUtil.formataHora(atendimento.getDataHoraFim(),
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
                MensagemUtil.getMensagem("push.confirmacao_agendamento.title", agendamento.getIgreja().getLocale()),
                MensagemUtil.getMensagem("push.confirmacao_agendamento.message", agendamento.getIgreja().getLocale(),
                        agendamento.getCalendario().getPastor().getNome(),
                        MensagemUtil.formataData(agendamento.getDataHoraInicio(),
                                agendamento.getIgreja().getLocale(),
                                agendamento.getIgreja().getTimezone()),
                        MensagemUtil.formataHora(agendamento.getDataHoraInicio(),
                                agendamento.getIgreja().getLocale(),
                                agendamento.getIgreja().getTimezone()),
                        MensagemUtil.formataHora(agendamento.getDataHoraFim(),
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
                agendamento.getCalendario().getPastor().getId().equals(sessaoBean.getIdMembro())){
            enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getIgreja(),
                    agendamento.getMembro().getId()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_pastor.title", agendamento.getIgreja().getLocale()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_pastor.message", agendamento.getIgreja().getLocale(),
                            agendamento.getCalendario().getPastor().getNome(),
                            MensagemUtil.formataData(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraFim(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone())),
                    TipoNotificacao.ACONSELHAMENTO, false);
        }else{
            enviaPush(new FiltroDispositivoNotificacaoDTO(agendamento.getIgreja(),
                    agendamento.getCalendario().getPastor().getId()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_membro.title", agendamento.getIgreja().getLocale()),
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_membro.message", agendamento.getIgreja().getLocale(),
                            agendamento.getMembro().getNome(),
                            MensagemUtil.formataData(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraInicio(),
                                    agendamento.getIgreja().getLocale(),
                                    agendamento.getIgreja().getTimezone()),
                            MensagemUtil.formataHora(agendamento.getDataHoraFim(),
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
        return daoService.create(evento);
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public Evento atualiza(Evento evento) {
        return daoService.update(evento);
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public Evento buscaEvento(Long evento) {
        Evento entidade = daoService.find(Evento.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), evento));
        entidade.setVagasRestantes(entidade.getLimiteInscricoes() - ((Number) daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento))).intValue());
        return entidade;
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public void removeEvento(Long evento) {
        Evento entidade = buscaEvento(evento);
        
        if (entidade != null){
            Number count = daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento));
            if (count.intValue() > 0){
                throw new ServiceException("mensagens.MSG-041");
            }
            
            daoService.execute(QueryAdmin.DELETE_INSCRICOES.create(evento));
            daoService.delete(Evento.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), entidade.getId()));
        }
    }
    
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public BuscaPaginadaDTO<Evento> buscaTodos(FiltroEventoDTO filtro) {
        return daoService.findWith(new FiltroEvento(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }
    
    @Override
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public BuscaPaginadaDTO<Evento> buscaFuturos(FiltroEventoFuturoDTO filtro) {
        return daoService.findWith(new FiltroEvento(sessaoBean.getChaveIgreja(), sessaoBean.isAdmin(), filtro));
    }
    
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public BuscaPaginadaDTO<InscricaoEvento> buscaTodas(Long evento, FiltroInscricaoDTO filtro) {
        return daoService.findWith(new FiltroInscricao(evento, sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), filtro));
    }
    
    @Override
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public BuscaPaginadaDTO<InscricaoEvento> buscaMinhas(Long evento, FiltroMinhasInscricoesDTO filtro) {
        return daoService.findWith(new FiltroInscricao(evento, sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro(), filtro));
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public void confirmaInscricao(Long evento, Long inscricao) {
        InscricaoEvento entidade = daoService.find(InscricaoEvento.class,
                new InscricaoEventoId(inscricao, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), evento)));
        
        entidade.confirmada();
        
        daoService.update(entidade);
    }
    
    @Audit
    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_EBD})
    public void cancelaInscricao(Long evento, Long inscricao) {
        InscricaoEvento entidade = daoService.find(InscricaoEvento.class,
                new InscricaoEventoId(inscricao, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), evento)));
        
        entidade.cancelada();
        
        daoService.update(entidade);
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario cadastra(VersiculoDiario versiculoDiario) {
        versiculoDiario.setIgreja(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()));
        Number minimo = daoService.findWith(QueryAdmin.MENOR_ENVIO_VERSICULOS.createSingle(sessaoBean.getChaveIgreja()));
        if (minimo != null){
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
        if (minimo != null){
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
        daoService.delete(VersiculoDiario.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), versiculoDiario));
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
    @AllowMembro({Funcionalidade.REALIZAR_INSCRICAO_EVENTO, Funcionalidade.REALIZAR_INSCRICAO_EBD})
    public ResultadoInscricaoDTO realizaInscricao(List<InscricaoEvento> inscricoes) {
        if (!inscricoes.isEmpty()) {
            Evento evento = inscricoes.get(0).getEvento();
            Membro membro = buscaMembro(sessaoBean.getIdMembro());
            
            

            Number qtde = daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento.getId()));
            if (qtde.intValue() + inscricoes.size() > evento.getLimiteInscricoes()){
                throw new ServiceException("mensagens.MSG-034");
            }
            
            List<InscricaoEvento> cadastradas = new ArrayList<InscricaoEvento>();
            for (InscricaoEvento inscricao : inscricoes) {
                inscricao.setMembro(membro);
                cadastradas.add(daoService.create(inscricao));
            }
            
            if (evento.isComPagamento()) {
                BigDecimal valorTotal = BigDecimal.ZERO;
                
                for (InscricaoEvento inscricao : cadastradas) {
                    valorTotal = valorTotal.add(inscricao.getValor());
                }
                
                ConfiguracaoIgrejaDTO configuracao = buscaConfiguracao();
                if (configuracao != null && configuracao.isHabilitadoPagSeguro()){
                    String referencia = sessaoBean.getChaveIgreja().toUpperCase() +
                            Long.toString(System.currentTimeMillis(), 36).toUpperCase();
                    
                    PagSeguroService.Pedido pedido = new PagSeguroService.Pedido(referencia,
                            new PagSeguroService.Solicitante(membro.getNome(), membro.getEmail()));
                    
                    for (InscricaoEvento inscricao : inscricoes){
                        pedido.add(new PagSeguroService.ItemPedido(
                                Long.toString(inscricao.getId(), 36).toUpperCase(),
                                MensagemUtil.getMensagem("pagseguro.inscricao.item", evento.getIgreja().getLocale(),
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
                    
                    String subject = MensagemUtil.getMensagem("email.pagamento_inscricao.subject", evento.getIgreja().getLocale());
                    String title = MensagemUtil.getMensagem("email.pagamento_inscricao.message.title", evento.getIgreja().getLocale(), membro.getNome());
                    String text = MensagemUtil.getMensagem("email.pagamento_inscricao.message.text", evento.getIgreja().getLocale(), evento.getNome(), nformat.format(pedido.getTotal()));
                    String url = MensagemUtil.getMensagem("email.pagamento_inscricao.message.link.url", evento.getIgreja().getLocale(), checkout);
                    String link = MensagemUtil.getMensagem("email.pagamento_inscricao.message.link.text", evento.getIgreja().getLocale());
                    
                    notificacaoService.sendNow(
                            MensagemUtil.email(recuperaInstitucional(), subject,
                                    new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, url, link), Collections.EMPTY_LIST)),
                            new FiltroEmailDTO(evento.getIgreja(), membro.getId()));
                    
                    return new ResultadoInscricaoDTO(checkout);
                }
            }
        }
        
        return new ResultadoInscricaoDTO();
    }
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public ConfiguracaoIgrejaDTO atualiza(ConfiguracaoIgrejaDTO configuracao) {
        ValidationException validation = ValidationException.build();
        
        if (!StringUtil.isEmpty(configuracao.getTituloAniversario()) &&
                configuracao.getTituloAniversario().length() > 30){
            validation.add("tituloAniversario", "mensagens.MSG-010");
        }
        
        if (!StringUtil.isEmpty(configuracao.getTituloBoletim()) &&
                configuracao.getTituloBoletim().length() > 30){
            validation.add("tituloBoletim", "mensagens.MSG-010");
        }
        
        if (!StringUtil.isEmpty(configuracao.getTituloVersiculoDiario()) &&
                configuracao.getTituloVersiculoDiario().length() > 30){
            validation.add("tituloVersiculoDiario", "mensagens.MSG-010");
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
    @AllowMembro(Funcionalidade.CONFIGURAR_YOUTUBE)
    public String buscaURLAutenticacaoYouTube() throws IOException {
        return googleService.getURLAutorizacaoYouTube();
    }

    @Override
    @AllowMembro(Funcionalidade.CONFIGURAR_YOUTUBE)
    public void iniciaConfiguracaoYouTube(String code) {
        try {
            googleService.saveCredentialsYouTube(code);
            
            ConfiguracaoYouTubeIgrejaDTO config = paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveIgreja());
            config.setIdCanal(googleService.buscaIdCanal());
            paramService.salvaConfiguracaoYouTube(config, sessaoBean.getChaveIgreja());
            
            Institucional institucional = recuperaInstitucional();
            if (!institucional.getRedesSociais().containsKey("youtube")){
                institucional.getRedesSociais().put("youtube", "https://www.youtube.com/channel/" + config.getIdCanal());
                daoService.update(institucional);
            }
        } catch (IOException ex) {
            Logger.getLogger(AppServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServiceException("mensagens.MSG-046");
        }
    }

    @Override
    public List<VideoDTO> buscaVideos() {
        try {
            return googleService.buscaVideos();
        } catch (IOException ex) {
            Logger.getLogger(AppServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
    }

    @Override
    @AllowMembro(Funcionalidade.CONFIGURAR_YOUTUBE)
    public ConfiguracaoYouTubeIgrejaDTO buscaConfiguracaoYouTube() {
        return paramService.buscaConfiguracaoYouTube(sessaoBean.getChaveIgreja());
    }

    @Schedule(hour = "*", minute = "0/15")
    public void verificaPagSeguro() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            ConfiguracaoIgrejaDTO configuracao = paramService.buscaConfiguracao(igreja.getChave());
            if (configuracao != null && configuracao.isPagSeguroConfigurado()){
                List<String> referencias = daoService.findWith(QueryAdmin.REFERENCIAS_INSCRICOES_PENDENTES.create(igreja.getChave()));
                
                for (String referencia : referencias){
                    atualizaSituacaoPagSeguro(referencia, configuracao);
                }
            }
        }
    }
    
    public void atualizaSituacaoPagSeguro(String referencia, ConfiguracaoIgrejaDTO configuracao){
        switch (pagSeguroService.getStatusPagamento(referencia, configuracao)){
            case PAGO:
            {
                List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.INSCRICOES_POR_REFERENCIA.create(referencia));
                
                if (!inscricoes.isEmpty()){
                    Membro membro = inscricoes.get(0).getMembro();
                    Evento evento = inscricoes.get(0).getEvento();
                    Institucional institucional = daoService.find(Institucional.class, evento.getIgreja().getChave());
                    BigDecimal total = BigDecimal.ZERO;
                    
                    for (InscricaoEvento inscricao : inscricoes){
                        inscricao.confirmada();
                        daoService.update(inscricao);
                        total = total.add(inscricao.getValor());
                    }
                    
                    Locale locale = Locale.forLanguageTag(evento.getIgreja().getLocale());
                    NumberFormat nformat = NumberFormat.getCurrencyInstance(locale);
                    
                    String subject = MensagemUtil.getMensagem("email.confirmacao_inscricao.subject", evento.getIgreja().getLocale());
                    String title = MensagemUtil.getMensagem("email.confirmacao_inscricao.message.title", evento.getIgreja().getLocale(), membro.getNome());
                    String text = MensagemUtil.getMensagem("email.confirmacao_inscricao.message.text", evento.getIgreja().getLocale(),
                            evento.getNome(), nformat.format(total), institucional.getEmail());
                    
                    notificacaoService.sendNow(
                            MensagemUtil.email(institucional, subject,
                                    new CalvinEmailDTO(null, Arrays.asList(new CalvinEmailDTO.Materia(title, text)))),
                            new FiltroEmailDTO(evento.getIgreja(), membro.getId()));
                }
                
            }
            break;
            case CANCELADO:
            {
                List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.INSCRICOES_POR_REFERENCIA.create(referencia));
                for (InscricaoEvento inscricao : inscricoes){
                    inscricao.cancelada();
                    daoService.update(inscricao);
                }
                break;
            }
        }
    }
    
    @Schedule(hour = "*")
    public void enviaVersiculos() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer hora = cal.get(Calendar.HOUR_OF_DAY);
            
            VersiculoDiario atual = daoService.findWith(QueryAdmin.VERSICULOS_POR_STATUS.createSingle(igreja.getId(), StatusVersiculoDiario.ATIVO));
            if (atual == null || !DateUtil.equalsSemHoras(atual.getUltimoEnvio(), new Date())){
                if (atual != null){
                    atual.habilitado();
                    atual = daoService.update(atual);
                }
                
                VersiculoDiario versiculo = daoService.findWith(QueryAdmin.SORTEIA_VERSICULO.createSingle(igreja.getId()));
                if (versiculo != null){
                    versiculo.ativo();
                    atual = daoService.update(versiculo);
                }
            }
            
            String titulo = paramService.get(igreja.getChave(), TipoParametro.TITULO_VERSICULO_DIARIO);
            if (StringUtil.isEmpty(titulo)){
                titulo = MensagemUtil.getMensagem("push.versiculo_diario.title", igreja.getLocale());
            }
            
            if (atual != null && atual.isAtivo()){
                for (HorasEnvioVersiculo hev : HorasEnvioVersiculo.values()){
                    if (hev.getHoraInt().equals(hora)){
                        enviaPush(new FiltroDispositivoNotificacaoDTO(igreja, hev), 
                                titulo, atual.getVersiculo(), TipoNotificacao.VERSICULO, true);
                        break;
                    }
                }
            }
        }
    }
    
    @Schedule(hour = "*")
    public void enviaParabensAniversario() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(igreja.getTimezone()));
            Integer hora = cal.get(Calendar.HOUR_OF_DAY);
            
            if (hora.equals(12)){
                List<Membro> aniversariantes = daoService.findWith(QueryAdmin.ANIVERSARIANTES.create(igreja.getChave()));
                
                for (Membro membro : aniversariantes){
                    String titulo = paramService.get(igreja.getChave(), TipoParametro.TITULO_ANIVERSARIO);
                    if (StringUtil.isEmpty(titulo)){
                        titulo = MensagemUtil.getMensagem("push.aniversario.title", igreja.getLocale());
                    }
                    
                    String texto = paramService.get(igreja.getChave(), TipoParametro.TEXTO_ANIVERSARIO);
                    if (StringUtil.isEmpty(texto)){
                        texto = MensagemUtil.getMensagem("push.aniversario.message",
                                igreja.getLocale(), membro.getNome(), igreja.getNome());
                    }else{
                        texto = MessageFormat.format(texto, membro.getNome(), igreja.getNome());
                    }
                    
                    enviaPush(new FiltroDispositivoNotificacaoDTO(igreja, membro.getId()), titulo, texto, TipoNotificacao.ANIVERSARIO, false);
                }
            }
        }
    }
    
    @Schedule(hour = "*")
    public void enviaNotificacoesBoletins() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS_COM_BOLETINS_A_DIVULGAR.create());
        for (Igreja igreja : igrejas) {
            String titulo = paramService.get(igreja.getChave(), TipoParametro.TITULO_BOLETIM);
            if (StringUtil.isEmpty(titulo)){
                titulo = MensagemUtil.getMensagem("push.boletim.title", igreja.getLocale());
            }
            
            String texto = paramService.get(igreja.getChave(), TipoParametro.TEXTO_BOLETIM);
            if (StringUtil.isEmpty(texto)){
                texto = MensagemUtil.getMensagem("push.boletim.message", igreja.getLocale(), igreja.getNome());
            }
            
            enviaPush(new FiltroDispositivoNotificacaoDTO(igreja), titulo, texto, TipoNotificacao.BOLETIM, false);
            
            daoService.execute(QueryAdmin.UPDATE_BOLETINS_NAO_DIVULGADOS.create(igreja.getChave()));
        }
    }

    @Schedule(hour = "*")
    public void enviaNotificacoesEstudos() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS_COM_ESTUDOS_A_DIVULGAR.create());
        for (Igreja igreja : igrejas) {
            String titulo = paramService.get(igreja.getChave(), TipoParametro.TITULO_ESTUDO);
            if (StringUtil.isEmpty(titulo)){
                titulo = MensagemUtil.getMensagem("push.estudo.title", igreja.getLocale());
            }

            String texto = paramService.get(igreja.getChave(), TipoParametro.TEXTO_ESTUDO);
            if (StringUtil.isEmpty(texto)){
                texto = MensagemUtil.getMensagem("push.estudo.message", igreja.getLocale(), igreja.getNome());
            }

            enviaPush(new FiltroDispositivoNotificacaoDTO(igreja), titulo, texto, TipoNotificacao.ESTUDO, false);

            daoService.execute(QueryAdmin.UPDATE_ESTUDOS_NAO_DIVULGADOS.create(igreja.getChave()));
        }
    }

    private void enviaPush(FiltroDispositivoNotificacaoDTO filtro, String titulo, String mensagem, TipoNotificacao tipo, boolean compartilhavel) {
        notificacaoService.sendNow(new MensagemPushDTO(titulo, mensagem, null, null, null, 
                new QueryParameters("tipo", tipo).set("compartilhavel", compartilhavel)), filtro);
    }
    
    private class ProcessamentoBoletim implements ProcessamentoService.Processo {
        
        private final Boletim boletim;
        private final RegistroIgrejaId bid;
        
        public ProcessamentoBoletim(Boletim boletim) {
            this.boletim = boletim;
            this.bid = new RegistroIgrejaId(boletim.getChaveIgreja(), boletim.getId());
        }
        
        @Override
        public void begin() {
            Boletim.lock(bid, 0);
        }
        
        @Override
        public boolean execute(int count) throws Exception {
            int total = trataPaginasPDF(boletim, count);
            int current = boletim.getPaginas().size();
            daoService.update(boletim);
            Boletim.lock(bid, (100 * current) / total);
            return total == current;
        }
        
        @Override
        public void success() {
            Boletim.unlock(bid);
            daoService.execute(QueryAdmin.UPDATE_STATUS_BOLETIM.
                    create(boletim.getChaveIgreja(), boletim.getId(), StatusBoletim.PUBLICADO));
        }
        
        @Override
        public void fail() {
            Boletim.unlock(bid);
            daoService.execute(QueryAdmin.UPDATE_STATUS_BOLETIM.
                    create(boletim.getChaveIgreja(), boletim.getId(), StatusBoletim.REJEITADO));
        }
    }
    
}
