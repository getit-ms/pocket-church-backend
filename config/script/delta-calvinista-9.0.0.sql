create table tb_item_timeline (
    id_item_timeline bigint,
    titulo varchar(250),
    tipo varchar(50),
    data timestamp,
    status integer,
    chave_igreja varchar(50),
    id_thumbnail bigint,
    primary key (id_item_timeline, tipo, chave_igreja),
    constraint fk_thumbnail
        foreign key (id_thumbnail, chave_igreja)
            references tb_arquivo(id_arquivo, chave_igreja)
);

create index idx_timeline_data
    on tb_item_timeline(chave_igreja, status, data);

-- TODO ajustar insert
insert into tb_item_timeline(id_item_timeline, titulo, tipo, data, status, chave_igreja, id_thumbnail)
    select id_boletim, titulo, 'BOLETIM', data_publicacao,  from tb_boletim;
