package br.gafs.calvinista.dao;

import br.gafs.calvinista.entity.IItemEvento;
import br.gafs.calvinista.entity.ItemEvento;
import br.gafs.calvinista.entity.ItemEventoId;
import br.gafs.dao.CrudServiceBean;

import javax.ejb.Stateless;

@Stateless
public class CustomCrudServiceBean extends CrudServiceBean implements CustomDAOService {
    @Override
    public <T> T create(T t) {
        T output = super.create(t);

        if (output instanceof IItemEvento) {
            super.create(((IItemEvento) output).getItemEvento());
        }

        return output;
    }

    @Override
    public <T> T update(T t) {
        T output = super.update(t);

        if (output instanceof IItemEvento) {
            super.update(((IItemEvento) output).getItemEvento());
        }

        return output;
    }

    @Override
    public void delete(Class type, Object id) {
        if (type.isAssignableFrom(IItemEvento.class)) {
            IItemEvento entidade = (IItemEvento) super.find(type, id);
            ItemEvento itemEvento = entidade.getItemEvento();
            super.delete(ItemEvento.class, new ItemEventoId(
                    itemEvento.getId(), itemEvento.getChaveIgreja(), itemEvento.getTipo()
            ));
        }

        super.delete(type, id);
    }
}
