/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.FiltroBoletim;
import br.gafs.calvinista.dao.FiltroDispositivo;
import br.gafs.calvinista.dao.FiltroEstudo;
import br.gafs.calvinista.dao.FiltroEvento;
import br.gafs.calvinista.dao.FiltroHino;
import br.gafs.calvinista.dao.FiltroInscricao;
import br.gafs.calvinista.dao.FiltroMembro;
import br.gafs.calvinista.dao.FiltroMeusAgendamentos;
import br.gafs.calvinista.dao.FiltroPedidoOracao;
import br.gafs.calvinista.dao.FiltroVersiculoDiario;
import br.gafs.calvinista.dao.FiltroVotacao;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.dto.CalvinEmailDTO;
import br.gafs.calvinista.dto.EventoAgendaDTO;
import br.gafs.calvinista.dto.FiltroBoletimDTO;
import br.gafs.calvinista.dto.FiltroBoletimPublicadoDTO;
import br.gafs.calvinista.dto.FiltroDispositivoDTO;
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
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.dto.ResultadoVotacaoDTO;
import br.gafs.calvinista.dto.StatusAdminDTO;
import br.gafs.calvinista.entity.Acesso;
import br.gafs.calvinista.entity.AcessoId;
import br.gafs.calvinista.entity.AgendamentoAtendimento;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.entity.Boletim;
import br.gafs.calvinista.entity.CalendarioAtendimento;
import br.gafs.calvinista.entity.Estudo;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.Hino;
import br.gafs.calvinista.entity.HorarioAtendimento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.InscricaoEventoId;
import br.gafs.calvinista.entity.Institucional;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Notificacao;
import br.gafs.calvinista.entity.Opcao;
import br.gafs.calvinista.entity.PedidoOracao;
import br.gafs.calvinista.entity.Perfil;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.RespostaVotacao;
import br.gafs.calvinista.entity.VersiculoDiario;
import br.gafs.calvinista.entity.Votacao;
import br.gafs.calvinista.entity.Voto;
import br.gafs.calvinista.entity.domain.DiaSemana;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.HorasEnvioVersiculo;
import br.gafs.calvinista.entity.domain.StatusPedidoOracao;
import br.gafs.calvinista.entity.domain.StatusVersiculoDiario;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.AllowMembro;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.ArquivoService;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.util.MensagemUtil;
import br.gafs.calvinista.util.PDFToImageConverterUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.dao.QueryParameters;
import br.gafs.exceptions.ServiceException;
import br.gafs.file.EntityFileManager;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.util.date.DateUtil;
import br.gafs.util.image.ImageUtil;
import br.gafs.util.senha.SenhaUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

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
                    acessoService.getIgreja(), 
                    MensagemUtil.email(recuperaInstitucional(), subject,
                            new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, "", senha), Collections.EMPTY_LIST)), 
                                Arrays.asList(entidade.getEmail()));
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
        Acesso acesso = buscaAcessoAdmin(membro);
        if (acesso == null) {
            acesso = new Acesso(darAcessoMembro(membro));
        }

        if (acesso.getMembro().equals(acessoService.getMembro())) {
            throw new ServiceException("mensagens.MSG-015");
        }

        acesso.getPerfis().clear();
        for (Perfil p : perfis) {
            acesso.getPerfis().add(buscaPerfil(p.getId()));
        }

        acesso.getMinisterios().clear();
        for (Ministerio m : ministerios) {
            acesso.getMinisterios().add(buscaMinisterio(m.getId()));
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
        trataBoletim(boletim);
        return daoService.create(boletim);
    }

    private void trataBoletim(final Boletim boletim) throws IOException {
        if (!boletim.getBoletim().isUsed()) {
            if (boletim.getId() != null){
                Boletim old = buscaBoletim(boletim.getId());
                arquivoService.registraDesuso(old.getBoletim().getId());
                arquivoService.registraDesuso(old.getThumbnail().getId());

                boletim.getPaginas().clear();
                List<Arquivo> pages = new ArrayList<Arquivo>(boletim.getPaginas());
                for (Arquivo arq : pages) {
                    arquivoService.registraDesuso(arq.getId());
                }
            }
            
            arquivoService.registraUso(boletim.getBoletim().getId());

            PDFToImageConverterUtil.convert(EntityFileManager.
                    get(boletim.getBoletim(), "dados")).forEachPage(new PDFToImageConverterUtil.PageHandler() {
                        @Override
                        public void handle(int page, byte[] dados) throws IOException {
                            if (page == 0){
                                boletim.setThumbnail(arquivoService.upload(boletim.getBoletim().getNome().
                                    replaceFirst(".[pP][dD][fF]$", "") + "_thumbnail.png", ImageUtil.redimensionaImagem(dados, 500, 500)));
                                arquivoService.registraUso(boletim.getThumbnail().getId());
                                boletim.getThumbnail().clearDados();
                            }
                            
                            Arquivo pagina = arquivoService.upload(boletim.getBoletim().getNome().
                                    replaceFirst(".[pP][dD][fF]$", "") + "_page"
                                    + new DecimalFormat("00000").format(page + 1) + ".png", dados);
                            boletim.getPaginas().add(pagina);
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
        trataBoletim(boletim);
        return daoService.update(boletim);
    }

    @Override
    public Boletim buscaBoletim(Long boletim) {
        return daoService.find(Boletim.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), boletim));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public void removeBoletim(Long boletim) {
        Boletim entidade = buscaBoletim(boletim);
        
        for (Arquivo page : entidade.getPaginas()) {
            arquivoService.registraDesuso(page.getId());
        }
        
        arquivoService.registraDesuso(entidade.getBoletim().getId());
        daoService.delete(Boletim.class, new RegistroIgrejaId(acessoService.getIgreja().getChave(), entidade.getId()));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_BOLETINS)
    public BuscaPaginadaDTO<Boletim> buscaTodos(FiltroBoletimDTO filtro) {
        return daoService.findWith(new FiltroBoletim(acessoService.getDispositivo(), filtro));
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
        return daoService.findWith(new FiltroVotacao(acessoService.getMembro(), acessoService.getIgreja(), filtro));
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
    @AllowAdmin(Funcionalidade.CONSULTAR_PEDIDOS_ORACAO)
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
    public AgendamentoAtendimento confirma(Long id) {
        AgendamentoAtendimento agendamento = buscaAgendamento(id);
        agendamento.confirmado();
        agendamento =daoService.update(agendamento);
        
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
    public String realizaInscricao(List<InscricaoEvento> inscricoes) {
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

                String checkout = "";// TODO realiza o checkout no PagSeguro

                for (InscricaoEvento inscricao : cadastradas) {
                    inscricao.setChaveCheckout(checkout);
                    daoService.update(inscricao);
                }

                return checkout;
            }
        }

        return null;
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
            
            if (atual != null && atual.isAtivo()){
                for (HorasEnvioVersiculo hev : HorasEnvioVersiculo.values()){
                    if (hev.getHoraInt().equals(hora)){
                        enviaPush(igreja, new FiltroDispositivoDTO(igreja, hev),  
                                MensagemUtil.getMensagem("push.versiculo_diario.title", igreja.getLocale()),
                                atual.getVersiculo());
                        break;
                    }
                }
            }
        }
    }

    private void enviaPush(FiltroDispositivoDTO filtro, 
            String titulo, String mensagem) {
        enviaPush(acessoService.getIgreja(), filtro, titulo, mensagem);
    }
    
    private void enviaPush(Igreja igreja, FiltroDispositivoDTO filtro, 
            String titulo, String mensagem) {
        BuscaPaginadaDTO<String> dispositivos;
        
        do{
            dispositivos = daoService.findWith(new FiltroDispositivo(filtro));
            
            notificacaoService.sendNow(igreja, 
                    new MensagemPushDTO(titulo, mensagem, null, null, null), 
                    dispositivos.getResultados());
        }while(dispositivos.isHasProxima());
    }

}
