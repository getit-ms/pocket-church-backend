package br.gafs.calvinista.servidor.pagseguro;

import br.com.uol.pagseguro.domain.*;
import br.com.uol.pagseguro.domain.checkout.Checkout;
import br.com.uol.pagseguro.enums.Currency;
import br.com.uol.pagseguro.enums.ShippingType;
import br.com.uol.pagseguro.exception.PagSeguroServiceException;
import br.com.uol.pagseguro.service.NotificationService;
import br.com.uol.pagseguro.service.TransactionSearchService;
import br.gafs.calvinista.dto.ConfiguracaoIgrejaDTO;
import br.gafs.dto.DTO;
import br.gafs.exceptions.ServiceException;
import lombok.*;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class PagSeguroService {

    public enum StatusPagamento {
        NENHUM,
        AGUARDANDO_PAGAMENTO,
        PAGO,
        CANCELADO
    }

    public StatusPagamento getStatusPagamento(String referencia, ConfiguracaoIgrejaDTO configuracao) {
        try {
            TransactionSearchResult searchByReference = TransactionSearchService.
                    searchByReference(getCredentialsPagSeguro(configuracao), referencia);

            Date data = null;
            int status = 0;
            for (TransactionSummary transaction : searchByReference.getTransactionSummaries()){
                if (transaction.getReference().equals(referencia)){
                    if (data == null || (transaction.getLastEvent() != null &&
                            transaction.getLastEvent().after(data))){
                        data = transaction.getLastEvent();
                    }

                    switch (transaction.getStatus()){
                        case INITIATED:
                        case IN_ANALYSIS:
                            status = status | 1;
                            break;
                        case PAID:
                            status = status | 4;
                            break;
                        case CANCELLED:
                            status = status | 2;
                            break;
                    }
                }
            }

            if ((status & 4) != 0){
                Logger.getLogger(PagSeguroService.class.
                        getName()).log(Level.INFO, "Pagamento "+referencia+" marcado como PAGO pelo PagSeguro.");
                return StatusPagamento.PAGO;
            }else if ((status & 1) != 0){
                return StatusPagamento.AGUARDANDO_PAGAMENTO;
            }else if ((status & 2) != 0){
                Logger.getLogger(PagSeguroService.class.
                        getName()).log(Level.INFO, "Pagamento "+referencia+" marcado como CANCELADO pelo PagSeguro.");
                return StatusPagamento.CANCELADO;
            }

        } catch (NullPointerException ex) {
            // PROBLEMA DA LIB PAGSEGURO
        } catch (PagSeguroServiceException ex) {
            Logger.getLogger(PagSeguroService.class.
                    getName()).log(Level.SEVERE, null, ex);
        }

        return StatusPagamento.NENHUM;
    }

    public String buscaReferenciaPorCodigo(String codigo, ConfiguracaoIgrejaDTO credenciais) {
        try {
            Transaction transaction = NotificationService.
                    checkTransaction(getCredentialsPagSeguro(credenciais), codigo);
            return transaction.getReference();
        } catch (PagSeguroServiceException ex) {
            Logger.getLogger(PagSeguroService.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public String buscaReferenciaIdTransacao(String idTransacao, ConfiguracaoIgrejaDTO credenciais) {
        try {
            Transaction transaction = TransactionSearchService.
                    searchByCode(getCredentialsPagSeguro(credenciais), idTransacao);
            return transaction.getReference();
        } catch (PagSeguroServiceException ex) {
            Logger.getLogger(PagSeguroService.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private String limit(String nome){
        if (nome.length() > 100){
            return nome.substring(0, 97) + "...";
        }

        return nome;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Pedido implements DTO {
        private List<ItemPedido> itens = new ArrayList<ItemPedido>();

        @NonNull
        private String codigo;

        @NonNull
        private Solicitante solicitante;

        @Setter
        private BigDecimal desconto;

        @Setter
        private BigDecimal frete;

        public void add(ItemPedido item){
            itens.add(item);
        }
        
        public BigDecimal getTotal(){
            BigDecimal total = BigDecimal.ZERO;
            
            for (ItemPedido item : itens){
                total = total.add(item.getValor());
            }
            
            return total;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Solicitante implements DTO {
        private String nome;
        private String email;
    }

    @Getter
    @AllArgsConstructor
    public static class ItemPedido implements DTO {
        private String codigo;
        private String nome;
        private Integer quantidade;
        private BigDecimal valor;
    }

    public String realizaCheckout(Pedido pedido, ConfiguracaoIgrejaDTO credenciais){
        Checkout checkout = new Checkout();
        for (ItemPedido item : pedido.getItens()){
            checkout.addItem(
                    item.getCodigo(),
                    limit(item.getNome()),
                    item.getQuantidade(),
                    item.getValor(),
                    0l, new BigDecimal("0.00")
            );
        }
        if (pedido.getDesconto() != null){
            checkout.setExtraAmount(pedido.getDesconto().negate());
        }
        checkout.setShippingCost(pedido.getFrete());
        checkout.setShippingType(ShippingType.NOT_SPECIFIED);
        checkout.setSender(new Sender(pedido.getSolicitante().getNome().contains(" ")
                ? pedido.getSolicitante().getNome() : pedido.getSolicitante().getNome() + " do GET IT",
                pedido.getSolicitante().getEmail()));
        checkout.setCurrency(Currency.BRL);
        checkout.setMaxUses(new BigInteger("5"));
        checkout.setReference(pedido.getCodigo());
        checkout.getParameter().addItem(new ParameterItem("paymentMode", "default"));

        try {
            return checkout.register(getCredentialsPagSeguro(credenciais), true);
        } catch (PagSeguroServiceException ex) {
            ex.printStackTrace();
            throw new ServiceException("mensagens.MSG-607", ex);
        }
    }

    private Credentials getCredentialsPagSeguro(ConfiguracaoIgrejaDTO configuracao) throws PagSeguroServiceException {
        return new AccountCredentials(configuracao.getUserPagSeguro(),
                configuracao.getTokenPagSeguro(), configuracao.getTokenPagSeguro());
    }
}
