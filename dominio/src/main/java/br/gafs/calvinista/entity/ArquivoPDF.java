package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;

import java.util.List;

public interface ArquivoPDF extends IEntity {
    Long getId();

    Arquivo getPDF();

    Arquivo getThumbnail();

    void setThumbnail(Arquivo arquivo);

    List<Arquivo> getPaginas();

    void setPaginas(List<Arquivo> arquivos);

    Igreja getIgreja();
}
