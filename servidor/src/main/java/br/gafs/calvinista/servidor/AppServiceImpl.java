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
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.AllowMembro;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.ArquivoService;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.mensagem.EmailService;
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

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import org.apache.commons.httpclient.util.ParameterParser;

/**
 *
 * @author Gabriel
 */
@Stateless
@Local(AppService.class)
@Interceptors({ServiceLoggerInterceptor.class, SecurityInterceptor.class})
public class AppServiceImpl implements AppService {

    @EJB
    private DAOService daoService;

    @EJB
    private AcessoService acessoService;
    
    @EJB
    private ArquivoService arquivoService;
    
    @EJB
    private MensagemService notificacaoService;
    
    @EJB
    private PagSeguroService pagSeguroService;
    
    @EJB
    private ParametroService  paramService; 
    
    @Override
    @AllowAdmin
    public StatusAdminDTO buscaStatus(){
        StatusAdminDTO status = new StatusAdminDTO();
        status.setVersiculoDiario(buscaVersiculoDiario());
        
        List<Funcionalidade> funcionalidades = acessoService.getFuncionalidades();
        if (funcionalidades.contains(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)){
            Number pedidos = daoService.findWith(new FiltroPedidoOracao(null, acessoService.getIgreja(), 
                    new FiltroPedidoOracaoDTO(null, null, Arrays.asList(StatusPedidoOracao.PENDENTE), 1, 10)).getCountQuery());

            if (pedidos.intValue() > 0){
                status.addNotificacao("mensagens.MSG-036", 
                        new QueryParameters("quantidade", pedidos));
            }
        }
        
        
        return status;
    }

    @Override
    @AllowAdmin
    @AllowMembro
    public List<ReleaseNotes> buscaReleaseNotes(TipoVersao tipo) {
        return daoService.findWith(QueryAdmin.RELEASE_NOTES.create(tipo));
    }

    private VersiculoDiario buscaVersiculoDiario(){
        return daoService.findWith(QueryAdmin.VERSICULOS_POR_STATUS.
                createSingle(acessoService.getIgreja().getChave(), StatusVersiculoDiario.ATIVO));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public List<Membro> buscaPastores() {
        return daoService.findWith(QueryAdmin.PASTORES_ATIVOS.
                create(acessoService.getIgreja().getChave()));
    }

    @Override
    public Chamado solicita(Chamado chamado) {
        if (acessoService.getDispositivo().isAdministrativo()){
            chamado.setTipo(TipoChamado.SUPORTE);
            
            if (acessoService.getMembro() == null || !acessoService.getMembro().
                            getAcesso().possuiPermissao(Funcionalidade.ABERTURA_CHAMADO_SUPORTE)){
                throw new ServiceException("mensagens.MSG-403");
            }
        }
        
        chamado.setDispositivoSolicitante(acessoService.getDispositivo());
        chamado = daoService.create(chamado);
        
        EmailUtil.sendMail(
                MessageFormat.format(ResourceBundleUtil._default().getPropriedade("CHAMADO_MESSAGE"),
                        chamado.getDescricao(), chamado.getIgrejaSolicitante().getNome(), 
                        chamado.getMembroSolicitante() != null ? chamado.getMembroSolicitante().getNome() : "",
                        chamado.getDispositivoSolicitante().getUuid(),
                        chamado.getDispositivoSolicitante().getVersao()), 
                MessageFormat.format(ResourceBundleUtil._default().getPropriedade("CHAMADO_SUBJECT"), 
                        chamado.getIgrejaSolicitante().getChave().toUpperCase(), 
                        chamado.getCodigo(), chamado.getTipo().name()),
                ResourceBundleUtil._default().getPropriedade("CHAMADO_MAIL").split("\\s*,\\s*"));
        
        return chamado;
    }

    @Override
    public BuscaPaginadaDTO<Chamado> busca(FiltroChamadoDTO filtro) {
        return daoService.findWith(new FiltroChamado(acessoService.getDispositivo(), filtro));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Membro cadastra(Membro membro) {
        membro.setIgreja(acessoService.getIgreja());
        return daoService.create(membro);
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public Membro darAcessoMembro(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (entidade.equals(acessoService.getMembro())) {
            throw new ServiceException("mensagens.MSG-015");
        }

        boolean gerarSenha = entidade.isSenhaUndefined();
        
        entidade.membro();
        entidade = daoService.update(entidade);
        
        if (gerarSenha){
            String senha = SenhaUtil.geraSenha(8);
            
            entidade.setSenha(SenhaUtil.encryptSHA256(senha));
            
            entidade = daoService.update(entidade);
            
            String subject = MensagemUtil.getMensagem("email.dar_acesso.subject", acessoService.getIgreja().getLocale());
            String title = MensagemUtil.getMensagem("email.dar_acesso.message.title", acessoService.getIgreja().getLocale(), 
                    entidade.getNome());
            String text = MensagemUtil.getMensagem("email.dar_acesso.message.text", acessoService.getIgreja().getLocale(), 
                    acessoService.getIgreja().getNome());
            
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
        return acessoService.getIgreja().getFuncionalidadesAplicativo();
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public List<Funcionalidade> getFuncionalidadesAplicativo() {
        return acessoService.getIgreja().getPlano().getFuncionalidadesMembro();
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_FUNCIONALIDADES_APLICATIVO)
    public void salvaFuncionalidadesHabilitadasAplicativo(List<Funcionalidade> funcionalidades) {
        Igreja igreja = acessoService.getIgreja();
        igreja.setFuncionalidadesAplicativo(funcionalidades);
        daoService.update(igreja);
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public Membro retiraAcessoMembro(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (entidade.equals(acessoService.getMembro())) {
            throw new ServiceException("mensagens.MSG-015");
        }

        entidade.contato();
        return daoService.update(entidade);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Acesso buscaAcessoAdmin(Long membro) {
        return daoService.find(Acesso.class, new AcessoId(new RegistroIgrejaId(
                acessoService.getIgreja().getChave(), membro)));
    }

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
        return daoService.findWith(new FiltroHino(acessoService.getIgreja(), filtro));
    }

    @Override
    public Hino buscaHino(Long hino) {
        return daoService.find(Hino.class, hino);
    }

    @Override
    public Opcao buscaOpcao(Long id) {
        return daoService.find(Opcao.class, id);
    }

    @Override
    public Questao buscaQuestao(Long id) {
        return daoService.find(Questao.class, id);
    }

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
            if (entidade.equals(acessoService.getMembro())) {
                throw new ServiceException("mensagens.MSG-015");
            }
        }

        return daoService.update(acesso);
    }

    @Override
    @AllowAdmin(Funcionalidade.GERENCIAR_ACESSO_MEMBROS)
    public void retiraAcessoAdmin(Long membro) {
        Membro entidade = buscaMembro(membro);

        if (entidade.equals(acessoService.getMembro())) {
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
                acessoService.getIgreja().getChave(),
                acessoService.getMembro().getId()));
    }

    @Override
    public Igreja buscaPorChave(String chave) {
        return daoService.find(Igreja.class, chave);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Membro atualiza(Membro membro) {
        return daoService.update(membro);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    @AllowMembro(Funcionalidade.CONSULTAR_CONTATOS_IGREJA)
    public Membro buscaMembro(Long membro) {
        return daoService.find(Membro.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), membro));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    @AllowMembro(Funcionalidade.CONSULTAR_CONTATOS_IGREJA)
    public BuscaPaginadaDTO<Membro> busca(FiltroMembroDTO filtro) {
        return daoService.findWith(new FiltroMembro(acessoService.getIgreja(), filtro));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MINISTERIOS)
    public Ministerio cadastra(Ministerio ministerio) {
        ministerio.setIgreja(acessoService.getIgreja());
        return daoService.create(ministerio);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MINISTERIOS)
    public Ministerio atualiza(Ministerio ministerio) {
        return daoService.update(ministerio);
    }

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
                createSingle(acessoService.getIgreja().getChave(), idMinisterio));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MINISTERIOS)
    public List<Ministerio> buscaMinisterios() {
        return daoService.findWith(QueryAdmin.MINISTERIOS_ATIVOS.create(acessoService.getIgreja().getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public Perfil cadastra(Perfil perfil) {
        perfil.setIgreja(acessoService.getIgreja());
        return daoService.create(perfil);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public Perfil atualiza(Perfil perfil) {
        return daoService.update(perfil);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public Perfil buscaPerfil(Long perfil) {
        return daoService.find(Perfil.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), perfil));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_PERFIS)
    public void removePerfil(Long perfil) {
        daoService.delete(Perfil.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), perfil));
    }

    @Override
    @AllowAdmin({
        Funcionalidade.MANTER_PERFIS,
        Funcionalidade.MANTER_MEMBROS
    })
    public List<Perfil> buscaPerfis() {
        return daoService.findWith(QueryAdmin.PERFIS.create(acessoService.getIgreja().getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public Boletim cadastra(Boletim boletim) throws IOException {
        boletim.setIgreja(acessoService.getIgreja());
        boletim.setBoletim(arquivoService.buscaArquivo(boletim.getBoletim().getId()));
        trataPDF(boletim);
        return daoService.create(boletim);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_CIFRAS)
    public Cifra cadastra(Cifra cifra) throws IOException {
        cifra.setIgreja(acessoService.getIgreja());
        cifra.setCifra(arquivoService.buscaArquivo(cifra.getCifra().getId()));
        trataPDF(cifra);
        return daoService.create(cifra);
    }

    private void trataPDF(final ArquivoPDF pdf) throws IOException {
        if (!pdf.getPDF().isUsed()) {
            if (pdf.getId() != null){
                ArquivoPDF old = daoService.find(pdf.getClass(), new RegistroIgrejaId(acessoService.getIgreja().getChave(), pdf.getId()));
                arquivoService.registraDesuso(old.getPDF().getId());
                arquivoService.registraDesuso(old.getThumbnail().getId());

                pdf.getPaginas().clear();
                List<Arquivo> pages = new ArrayList<Arquivo>(pdf.getPaginas());
                for (Arquivo arq : pages) {
                    arquivoService.registraDesuso(arq.getId());
                }
            }
            
            arquivoService.registraUso(pdf.getPDF().getId());

            PDFToImageConverterUtil.convert(EntityFileManager.
                    get(pdf.getPDF(), "dados")).forEachPage(new PDFToImageConverterUtil.PageHandler() {
                        @Override
                        public void handle(int page, byte[] dados) throws IOException {
                            if (page == 0){
                                pdf.setThumbnail(arquivoService.upload(pdf.getPDF().getNome().
                                    replaceFirst(".[pP][dD][fF]$", "") + "_thumbnail.png", ImageUtil.redimensionaImagem(dados, 500, 500)));
                                arquivoService.registraUso(pdf.getThumbnail().getId());
                                pdf.getThumbnail().clearDados();
                            }
                            
                            Arquivo pagina = arquivoService.upload(pdf.getPDF().getNome().
                                    replaceFirst(".[pP][dD][fF]$", "") + "_page"
                                    + new DecimalFormat("00000").format(page + 1) + ".png", dados);
                            pdf.getPaginas().add(pagina);
                            arquivoService.registraUso(pagina.getId());
                            pagina.clearDados();
                        }
                    });
        }
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public Boletim atualiza(Boletim boletim) throws IOException {
        boletim.setBoletim(arquivoService.buscaArquivo(boletim.getBoletim().getId()));
        trataPDF(boletim);
        return daoService.update(boletim);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_CIFRAS)
    public Cifra atualiza(Cifra cifra) throws IOException {
        cifra.setCifra(arquivoService.buscaArquivo(cifra.getCifra().getId()));
        trataPDF(cifra);
        return daoService.update(cifra);
    }

    @Override
    public Boletim buscaBoletim(Long boletim) {
        return daoService.find(Boletim.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), boletim));
    }

    @Override
    public Cifra buscaCifra(Long cifra) {
        return daoService.find(Cifra.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), cifra));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public void removeBoletim(Long boletim) {
        Boletim entidade = buscaBoletim(boletim);
        
        for (Arquivo page : entidade.getPaginas()) {
            arquivoService.registraDesuso(page.getId());
        }
        
        arquivoService.registraDesuso(entidade.getBoletim().getId());
        arquivoService.registraDesuso(entidade.getThumbnail().getId());
        daoService.delete(Boletim.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_CIFRAS)
    public void removeCifra(Long cifra) {
        Cifra entidade = buscaCifra(cifra);

        for (Arquivo page : entidade.getPaginas()) {
            arquivoService.registraDesuso(page.getId());
        }

        arquivoService.registraDesuso(entidade.getCifra().getId());
        arquivoService.registraDesuso(entidade.getThumbnail().getId());
        daoService.delete(Cifra.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public BuscaPaginadaDTO<Boletim> buscaTodos(FiltroBoletimDTO filtro) {
        return daoService.findWith(new FiltroBoletim(acessoService.getDispositivo(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<Cifra> busca(FiltroCifraDTO filtro) {
        return daoService.findWith(new FiltroCifra(acessoService.getIgreja(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<Boletim> buscaPublicados(FiltroBoletimPublicadoDTO filtro) {
        return buscaTodos(filtro);
    }

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
        Institucional institucional = daoService.find(Institucional.class, acessoService.getIgreja().getChave());
        if (institucional == null) {
            institucional = new Institucional(acessoService.getIgreja());
        }
        return institucional;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public Estudo cadastra(Estudo estudo) {
        estudo.setIgreja(acessoService.getIgreja());
        estudo.setMembro(acessoService.getMembro());
        return daoService.create(estudo);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public Estudo atualiza(Estudo estudo) {
        return daoService.update(estudo);
    }

    @Override
    public Estudo buscaEstudo(Long estudo) {
        return daoService.find(Estudo.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), estudo));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public void removeEstudo(Long estudo) {
        Estudo entidade = buscaEstudo(estudo);
        daoService.delete(Estudo.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_ESTUDOS)
    public BuscaPaginadaDTO<Estudo> buscaTodos(FiltroEstudoDTO filtro) {
        return daoService.findWith(new FiltroEstudo(acessoService.getDispositivo(), filtro));
    }

    @Override
    public BuscaPaginadaDTO<Estudo> buscaPublicados(FiltroEstudoPublicadoDTO filtro) {
        return buscaTodos(filtro);
    }

    @Override
    @AllowAdmin(Funcionalidade.ENVIAR_NOTIFICACOES)
    public void enviar(Notificacao notificacao) {
        notificacao.setIgreja(acessoService.getIgreja());

        notificacao = daoService.create(notificacao);

        FiltroDispositivoDTO filtro = new FiltroDispositivoDTO(notificacao.getIgreja());
        filtro.setApenasMembros(notificacao.isApenasMembros());
        for (Ministerio m : notificacao.getMinisteriosAlvo()){
            filtro.getMinisterios().add(m.getId());
        }
        
        enviaPush(filtro, MensagemUtil.getMensagem("push.notificacao.title", 
                acessoService.getIgreja().getLocale(), acessoService.getIgreja().getNomeAplicativo()), notificacao.getMensagem());
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

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public Votacao cadastra(Votacao votacao) {
        votacao.setIgreja(acessoService.getIgreja());
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
        return daoService.find(Votacao.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), votacao));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public void removeVotacao(Long votacao) {
        Votacao entidade = buscaVotacao(votacao);
        
        if (entidade != null){
            daoService.execute(QueryAdmin.REMOVER_VOTOS.create(acessoService.getIgreja().getChave(), votacao));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_OPCAO.create(acessoService.getIgreja().getChave(), votacao));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_QUESTAO.create(acessoService.getIgreja().getChave(), votacao));
            daoService.execute(QueryAdmin.REMOVER_RESPOSTAS_VOTACAO.create(acessoService.getIgreja().getChave(), votacao));
        }
        
        daoService.delete(Votacao.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public BuscaPaginadaDTO<Votacao> buscaTodas(FiltroVotacaoDTO filtro) {
        return daoService.findWith(new FiltroVotacao(acessoService.getMembro(), filtro));
    }

    @Override
    @AllowMembro(Funcionalidade.REALIZAR_VOTACAO)
    public BuscaPaginadaDTO<Votacao> buscaAtivas(FiltroVotacaoAtivaDTO filtro) {
        return buscaTodas(filtro);
    }

    @Override
    @AllowMembro(Funcionalidade.REALIZAR_VOTACAO)
    public void realizarVotacao(RespostaVotacao resposta) {
        daoService.create(resposta);
        daoService.create(new Voto(resposta.getVotacao(), acessoService.getMembro()));
    }

    @Override
    @AllowAdmin(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)
    public PedidoOracao atende(Long pedidoOracao) {
        PedidoOracao entidade = daoService.find(PedidoOracao.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), pedidoOracao));
        entidade.atende(acessoService.getMembro());
        entidade = daoService.update(entidade);
        
        enviaPush(new FiltroDispositivoDTO(acessoService.getIgreja(), 
                entidade.getSolicitante().getId()), 
                MensagemUtil.getMensagem("push.atendimento_pedido_oracao.title", acessoService.getIgreja().getLocale()), 
            MensagemUtil.getMensagem("push.atendimento_pedido_oracao.message", acessoService.getIgreja().getLocale(),
                    MensagemUtil.formataDataHora(entidade.getDataSolicitacao(), acessoService.getIgreja().getLocale(), acessoService.getIgreja().getTimezone())));
        
        return entidade;
    }

    @Override
    @AllowAdmin(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)
    public BuscaPaginadaDTO<PedidoOracao> buscaTodos(FiltroPedidoOracaoDTO filtro) {
        return daoService.findWith(new FiltroPedidoOracao(acessoService.getMembro(), acessoService.getIgreja(), filtro));
    }

    @Override
    @AllowMembro(Funcionalidade.PEDIR_ORACAO)
    public BuscaPaginadaDTO<PedidoOracao> buscaMeus(FiltroMeusPedidoOracaoDTO filtro) {
        return buscaTodos(filtro);
    }

    @Override
    @AllowMembro(Funcionalidade.PEDIR_ORACAO)
    public PedidoOracao realizaPedido(PedidoOracao pedido) {
        pedido.setSolicitante(acessoService.getMembro());
        return daoService.create(pedido);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public AgendamentoAtendimento agenda(Long membro, Long idHorario, Date data) {
        if (!acessoService.getDispositivo().isAdministrativo()) {
            return _agenda(acessoService.getMembro(), idHorario, data);
        } else {
            return confirma(_agenda(daoService.find(Membro.class, new RegistroIgrejaId(acessoService.
                                getIgreja().getId(), membro)), idHorario, data).getId());
        }
    }

    private AgendamentoAtendimento _agenda(Membro membro, Long idHorario, Date data) {
        HorarioAtendimento horario = daoService.find(HorarioAtendimento.class, idHorario);

        if (!acessoService.getIgreja().equals(horario.getCalendario().getIgreja())) {
            throw new ServiceException("mensagens.MSG-604");
        }

        AgendamentoAtendimento atendimento = daoService.create(new AgendamentoAtendimento(membro, horario, data));
        
        enviaPush(new FiltroDispositivoDTO(acessoService.getIgreja(), atendimento.getCalendario().getPastor().getId()), 
                MensagemUtil.getMensagem("push.agendamento.title", acessoService.getIgreja().getLocale()), 
                MensagemUtil.getMensagem("push.agendamento.message", acessoService.getIgreja().getLocale(), 
                        atendimento.getMembro().getNome(), 
                        MensagemUtil.formataData(atendimento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(atendimento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(atendimento.getDataHoraFim(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone())));
        
        return atendimento;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public AgendamentoAtendimento confirma(Long id) {
        AgendamentoAtendimento agendamento = buscaAgendamento(id);
        
        if (!acessoService.getDispositivo().isAdministrativo()
                && !agendamento.getCalendario().getPastor().equals(acessoService.getMembro())) {
            throw new ServiceException("mensagens.MSG-604");
        }
        
        agendamento.confirmado();
        agendamento = daoService.update(agendamento);
        
        enviaPush(new FiltroDispositivoDTO(acessoService.getIgreja(), agendamento.getMembro().getId()), 
                MensagemUtil.getMensagem("push.confirmacao_agendamento.title", acessoService.getIgreja().getLocale()), 
                MensagemUtil.getMensagem("push.confirmacao_agendamento.message", acessoService.getIgreja().getLocale(), 
                        agendamento.getCalendario().getPastor().getNome(), 
                        MensagemUtil.formataData(agendamento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(agendamento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(agendamento.getDataHoraFim(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone())));
        
        return agendamento;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public AgendamentoAtendimento cancela(Long id) {
        AgendamentoAtendimento agendamento = buscaAgendamento(id);

        if (!acessoService.getDispositivo().isAdministrativo()
                && !agendamento.getMembro().equals(acessoService.getMembro())) {
            throw new ServiceException("mensagens.MSG-604");
        }

        agendamento.cancelado();
        
        agendamento = daoService.update(agendamento);
        
        if (!acessoService.getDispositivo().isAdministrativo()){
            enviaPush(new FiltroDispositivoDTO(acessoService.getIgreja(), 
                    agendamento.getCalendario().getPastor().getId()), 
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_membro.title", acessoService.getIgreja().getLocale()), 
                MensagemUtil.getMensagem("push.cancelamento_agendamento_membro.message", acessoService.getIgreja().getLocale(), 
                        agendamento.getMembro().getNome(), 
                        MensagemUtil.formataData(agendamento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(agendamento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(agendamento.getDataHoraFim(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone())));
        }else{
            enviaPush(new FiltroDispositivoDTO(acessoService.getIgreja(), 
                    agendamento.getMembro().getId()), 
                    MensagemUtil.getMensagem("push.cancelamento_agendamento_pastor.title", acessoService.getIgreja().getLocale()), 
                MensagemUtil.getMensagem("push.cancelamento_agendamento_pastor.message", acessoService.getIgreja().getLocale(), 
                        agendamento.getCalendario().getPastor().getNome(), 
                        MensagemUtil.formataData(agendamento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(agendamento.getDataHoraInicio(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone()), 
                        MensagemUtil.formataHora(agendamento.getDataHoraFim(), 
                                acessoService.getIgreja().getLocale(), 
                                acessoService.getIgreja().getTimezone())));
        }
        
        return agendamento;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public AgendamentoAtendimento buscaAgendamento(Long agendamento) {
        return daoService.find(AgendamentoAtendimento.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), agendamento));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public List<AgendamentoAtendimento> buscaAgendamentos(CalendarioAtendimento calendario, Date dataInicio, Date dataTermino) {
        Date dataAtual = DateUtil.getDataAtual();
        return daoService.findWith(QueryAdmin.AGENDAMENTOS_ATENDIMENTO.
                create(acessoService.getIgreja().getChave(), calendario.getId(), 
                dataInicio.before(dataAtual) ? dataAtual : dataInicio, dataTermino));
    }

    @Override
    @AllowMembro(Funcionalidade.AGENDAR_ACONSELHAMENTO)
    public BuscaPaginadaDTO<AgendamentoAtendimento> buscaMeusAgendamentos(FiltroMeusAgendamentoDTO filtro) {
        return daoService.findWith(new FiltroMeusAgendamentos(acessoService.getMembro(), filtro));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public CalendarioAtendimento cadastra(CalendarioAtendimento calendario) {
        calendario.setPastor(buscaMembro(calendario.getPastor().getId()));
        calendario.setIgreja(acessoService.getIgreja());
        return daoService.create(calendario);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public CalendarioAtendimento buscaCalendario(Long calendario) {
        return daoService.find(CalendarioAtendimento.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), calendario));
    }

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
        return daoService.findWith(QueryAdmin.CALENDARIOS.create(acessoService.getIgreja().getId()));
    }

    private HorarioAtendimento buscaHorario(Long calendario, Long horario) {
        HorarioAtendimento h = daoService.find(HorarioAtendimento.class, horario);

        if (!h.getCalendario().getId().equals(calendario)) {
            throw new ServiceException("mensagens.MSG-604");
        }

        return h;
    }

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

    @Override
    @AllowAdmin(Funcionalidade.MANTER_AGENDA)
    public void removeDia(Long calendario, Long idHorario, Date data) {
        removePeriodo(calendario, idHorario, data, data);
    }

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

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(calendario.getIgreja().getTimezone()));
        for (Date data = dataInicio;
                DateUtil.compareSemHoras(data, dataTermino) <= 0; 
                data = DateUtil.incrementaDias(data, 1)) {
            cal.setTime(data);
            for (HorarioAtendimento horario : horarios) {
                if (horario.contains(cal)) {
                    Date dti = horario.getInicio(data);
                    Date dtf = horario.getFim(data);
                    if (dtf.after(dataInicio) && dti.before(dataTermino) 
                            && !temAgendamento(calendario, dti, dtf)) {
                        eventos.add(new EventoAgendaDTO(dti, dtf, horario));
                    }
                }
            }
        }
        return eventos;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_EVENTOS)
    public Evento cadastra(Evento evento) {
        evento.setIgreja(acessoService.getIgreja());
        return daoService.create(evento);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_EVENTOS)
    public Evento atualiza(Evento evento) {
        return daoService.update(evento);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_EVENTOS)
    @AllowMembro(Funcionalidade.REALIZAR_INSCRICAO_EVENTO)
    public Evento buscaEvento(Long evento) {
        Evento entidade = daoService.find(Evento.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), evento));
        entidade.setVagasRestantes(entidade.getLimiteInscricoes() - ((Number) daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento))).intValue());
        return entidade;
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_EVENTOS)
    public void removeEvento(Long evento) {
        Evento entidade = buscaEvento(evento);
        
        if (entidade != null){
            Number count = daoService.findWith(QueryAdmin.BUSCA_QUANTIDADE_INSCRICOES.createSingle(evento));
            if (count.intValue() > 0){
                throw new ServiceException("mensagens.MSG-041");
            }
            
            daoService.execute(QueryAdmin.DELETE_INSCRICOES.create(evento));
            daoService.delete(Evento.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), entidade.getId()));
        }
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_EVENTOS)
    public BuscaPaginadaDTO<Evento> buscaTodos(FiltroEventoDTO filtro) {
        return daoService.findWith(new FiltroEvento(acessoService.getDispositivo(), filtro));
    }

    @Override
    @AllowMembro(Funcionalidade.REALIZAR_INSCRICAO_EVENTO)
    public BuscaPaginadaDTO<Evento> buscaFuturos(FiltroEventoFuturoDTO filtro) {
        return daoService.findWith(new FiltroEvento(acessoService.getDispositivo(), filtro));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_EVENTOS)
    public BuscaPaginadaDTO<InscricaoEvento> buscaTodas(Long evento, FiltroInscricaoDTO filtro) {
        return daoService.findWith(new FiltroInscricao(evento, acessoService.getIgreja(), acessoService.getMembro(), filtro));
    }

    @Override
    @AllowMembro(Funcionalidade.REALIZAR_INSCRICAO_EVENTO)
    public BuscaPaginadaDTO<InscricaoEvento> buscaMinhas(Long evento, FiltroMinhasInscricoesDTO filtro) {
        return daoService.findWith(new FiltroInscricao(evento, acessoService.getIgreja(), acessoService.getMembro(), filtro));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_EVENTOS)
    public void confirmaInscricao(Long evento, Long inscricao) {
        InscricaoEvento entidade = daoService.find(InscricaoEvento.class, 
                new InscricaoEventoId(inscricao, new RegistroIgrejaId(acessoService.getIgreja().getChave(), evento)));

        entidade.confirmada();
        
        daoService.update(entidade);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario cadastra(VersiculoDiario versiculoDiario) {
        versiculoDiario.setIgreja(acessoService.getIgreja());
        Number minimo = daoService.findWith(QueryAdmin.MENOR_ENVIO_VERSICULOS.createSingle(acessoService.getIgreja().getChave()));
        if (minimo != null){
            versiculoDiario.setEnvios(minimo.intValue());
        }
        return daoService.create(versiculoDiario);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario desabilita(Long versiculo) {
        VersiculoDiario entidade = buscaVersiculo(versiculo);
        entidade.desabilitado();
        return daoService.update(entidade);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario habilita(Long versiculo) {
        VersiculoDiario entidade = buscaVersiculo(versiculo);
        entidade.habilitado();
        Number minimo = daoService.findWith(QueryAdmin.MENOR_ENVIO_VERSICULOS.createSingle(acessoService.getIgreja().getChave()));
        if (minimo != null){
            entidade.setEnvios(minimo.intValue());
        }
        return daoService.update(entidade);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public VersiculoDiario buscaVersiculo(Long versiculoDiario) {
        return daoService.find(VersiculoDiario.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), versiculoDiario));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public void removeVersiculo(Long versiculoDiario) {
        VersiculoDiario entidade = buscaVersiculo(versiculoDiario);
        daoService.delete(VersiculoDiario.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VERSICULOS_DIARIOS)
    public BuscaPaginadaDTO<VersiculoDiario> busca(FiltroVersiculoDiarioDTO filtro) {
        return daoService.findWith(new FiltroVersiculoDiario(acessoService.getIgreja(), filtro));
    }

    private boolean temAgendamento(CalendarioAtendimento calendario, Date dti, Date dtf) {
        return daoService.findWith(QueryAdmin.AGENDAMENTO_EM_CHOQUE.createSingle(calendario.getId(), dti, dtf)) != null;
    }

    @Override
    @AllowMembro(Funcionalidade.REALIZAR_INSCRICAO_EVENTO)
    public ResultadoInscricaoDTO realizaInscricao(List<InscricaoEvento> inscricoes) {
        if (!inscricoes.isEmpty()) {
            Evento evento = inscricoes.get(0).getEvento();

            List<InscricaoEvento> cadastradas = new ArrayList<InscricaoEvento>();
            for (InscricaoEvento inscricao : inscricoes) {
                cadastradas.add(daoService.create(inscricao));
            }

            if (evento.isComPagamento()) {
                BigDecimal valorTotal = BigDecimal.ZERO;

                for (InscricaoEvento inscricao : cadastradas) {
                    valorTotal = valorTotal.add(inscricao.getValor());
                }

                ConfiguracaoIgrejaDTO configuracao = buscaConfiguracao();
                if (configuracao != null && configuracao.isHabilitadoPagSeguro()){
                    String referencia = acessoService.getIgreja().getChave().toUpperCase() +
                            Long.toString(System.currentTimeMillis(), 36);

                    PagSeguroService.Pedido pedido = new PagSeguroService.Pedido(referencia,
                            new PagSeguroService.Solicitante(
                                    acessoService.getMembro().getNome(),
                                    acessoService.getMembro().getEmail()));

                    for (InscricaoEvento inscricao : inscricoes){
                        pedido.add(new PagSeguroService.ItemPedido(
                            Long.toString(inscricao.getId(), 36),
                            MensagemUtil.getMensagem("pagseguro.inscricao.item", acessoService.getIgreja().getLocale(),
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

                    return new ResultadoInscricaoDTO(checkout);
                }
            }
        }

        return new ResultadoInscricaoDTO();
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public ConfiguracaoIgrejaDTO atualiza(ConfiguracaoIgrejaDTO configuracao) {
        paramService.salvaConfiguracao(configuracao, acessoService.getIgreja());
        return buscaConfiguracao();
    }

    @Override
    @AllowAdmin(Funcionalidade.CONFIGURAR)
    public ConfiguracaoIgrejaDTO buscaConfiguracao() {
        return paramService.buscaConfiguracao(acessoService.getIgreja());
    }

    @Schedule(hour = "*")
    public void verificaPagSeguro() {
        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());
        for (Igreja igreja : igrejas) {
            ConfiguracaoIgrejaDTO configuracao = buscaConfiguracao();
            if (configuracao != null && configuracao.isPagSeguroConfigurado()){
                List<String> referencias = daoService.findWith(QueryAdmin.BUSCA_REFERENCIAS_INSCRICOES_PENDENTES.create(igreja.getChave()));

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
                    for (InscricaoEvento inscricao : inscricoes){
                        inscricao.confirmada();
                        daoService.update(inscricao);
                    }
                }
                break;
            case CANCELADO:
                {
                    List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.INSCRICOES_POR_REFERENCIA.create(referencia));
                    for (InscricaoEvento inscricao : inscricoes){
                        inscricao.confirmada();
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
                        enviaPush(new FiltroDispositivoDTO(igreja, hev), titulo, atual.getVersiculo());
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
                String titulo = paramService.get(igreja.getChave(), TipoParametro.TITULO_ANIVERSARIO);
                if (StringUtil.isEmpty(titulo)){
                    titulo = MensagemUtil.getMensagem("push.aniversario.title", igreja.getLocale());
                }
                
                String texto = paramService.get(igreja.getChave(), TipoParametro.TEXTO_ANIVERSARIO);
                if (StringUtil.isEmpty(texto)){
                    texto = MensagemUtil.getMensagem("push.aniversario.message", igreja.getLocale(), igreja.getNome());
                }
                
                enviaPush(new FiltroDispositivoDTO(igreja, cal.getTime()), titulo, texto);
            }
        }
    }

    private void enviaPush(FiltroDispositivoDTO filtro, String titulo, String mensagem) {
        notificacaoService.sendNow(new MensagemPushDTO(titulo, mensagem, null, null, null), filtro);
    }

}
