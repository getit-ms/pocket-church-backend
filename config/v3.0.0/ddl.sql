create table tb_item_evento (
    id_item_evento varchar(150),
    chave_empresa varchar(50),
    tipo varchar(100),
    titulo varchar(150),
    apresentacao varchar(250),
    data_hora timestamp,
    status integer,
    id_ilustracao bigint,
    id_autor bigint,
    primary key (id_item_evento, chave_empresa, tipo),
    constraint fk_empresa_item_evento
        foreign key (chave_empresa)
            references tb_empresa(chave_empresa),
    constraint fk_ilustracao_item_evento
        foreign key (chave_empresa, id_ilustracao)
            references tb_arquivo(chave_empresa, id_arquivo),
    constraint fk_autor_item_evento
        foreign key (chave_empresa, id_autor)
            references tb_colaborador(chave_empresa, id_colaborador)
);

alter table tb_boletim
    add column id_autor bigint;

alter table tb_boletim
    add constraint fk_autor_boletim_informativo
        foreign key (chave_empresa, id_autor)
            references tb_colaborador(chave_empresa, id_colaborador);

alter table tb_documento
    rename column autor to autoria;

alter table tb_audio
    add column data_hora timestamp;

alter table tb_audio
    rename column autor to autoria;

alter table tb_audio
    add column id_autor bigint;

alter table tb_audio
    add constraint fk_autor_audio
        foreign key (chave_empresa, id_autor)
            references tb_colaborador(chave_empresa, id_colaborador);

create index idx_item_evento
    on tb_item_evento(chave_empresa, tipo, id_item_evento);

create index idx_item_evento_data
    on tb_item_evento(chave_empresa, status, data_hora);
