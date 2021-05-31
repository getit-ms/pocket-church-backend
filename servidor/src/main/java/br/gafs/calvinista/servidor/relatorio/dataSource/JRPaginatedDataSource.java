package br.gafs.calvinista.servidor.relatorio.dataSource;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.dao.BuscaPaginadaDTO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class JRPaginatedDataSource<F extends FiltroInscricaoDTO, E extends IEntity> extends JRAbstractBeanDataSource {

    private Searcher<E> searcher;
    private BuscaPaginadaDTO<E> pagina;
    private int index = 0;

    public JRPaginatedDataSource(Searcher<E> searcher) {
        super(true);
        this.searcher = searcher;
    }

    @Override
    public void moveFirst() {
        this.pagina = searcher.search(1);
        this.index = 0;
    }

    @Override
    public boolean next() {
        if (pagina == null) {
            moveFirst();
            return !pagina.isEmpty();
        } else if (index + 1 < pagina.getResultados().size()) {
            this.index++;
            return true;
        } else if (pagina.isHasProxima()) {
            this.pagina = searcher.search(pagina == null ? 1 : pagina.getPagina() + 1);
            this.index = 0;
            return true;
        }

        return false;
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        return getFieldValue(pagina.getResultados().get(index), jrField);
    }

    public interface Searcher<E> {
        BuscaPaginadaDTO<E> search(Integer pagina);
    }
}
