alter table tb_item_evento add column apresentacao type text;

create table tb_galeria_fotos (
    id_galeria_fotos varchar(150),
    chave_empresa varchar(50),
    nome varchar(150),
    descricao text,
    data_atualizacao timestamp,
    data_sincronizacao timestamp,
    foto_primaria text,
    quantidade_fotos integer,
    primary key (id_galeria_fotos, chave_empresa),
    constraint fk_empresa_galeria_fotos
        foreign key (chave_empresa)
            references tb_empresa(chave_empresa)
);
