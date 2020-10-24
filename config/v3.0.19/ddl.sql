create table tb_curtida_item_evento (
    id_colaborador bigint,
    id_item_evento varchar(150),
    chave_empresa varchar(50),
    tipo_item_evento varchar(50),
    data_hora timestamp,
    primary key (id_colaborador, id_item_evento, chave_empresa, tipo_item_evento),
    constraint fk_colaborador_curtida
        foreign key (id_colaborador, chave_empresa)
            references tb_colaborador(id_colaborador, chave_empresa),
    constraint fk_empresa_curtida
        foreign key (chave_empresa)
            references tb_empresa(chave_empresa)
);

create table tb_comentario_item_evento (
    id_comentario_item_evento bigint,
    id_colaborador bigint,
    id_item_evento varchar(150),
    chave_empresa varchar(50),
    tipo_item_evento varchar(50),
    data_hora timestamp,
    comentario text,
    status integer,
    primary key (id_comentario_item_evento, chave_empresa),
    constraint fk_colaborador_comentario
        foreign key (id_colaborador, chave_empresa)
            references tb_colaborador(id_colaborador, chave_empresa),
    constraint fk_empresa_comentario
        foreign key (chave_empresa)
            references tb_empresa(chave_empresa),
);

create sequence seq_comentario_item_evento increment by 50 start with 50;

create table tb_denuncia_comentario_item_evento (
    id_denuncia_comentario_item_evento bigint,
    id_denunciante bigint,
    id_analista bigint,
    id_comentario_item_evento bigint,
    chave_empresa varchar(50),
    data_hora_denuncia timestamp,
    data_hora_analise timestamp,
    justificativa text,
    status integer,
    primary key (id_denuncia_comentario_item_evento, chave_empresa),
    constraint fk_denunciante_denuncia
        foreign key (id_denunciante, chave_empresa)
            references tb_colaborador(id_colaborador, chave_empresa),
    constraint fk_analista_denuncia
        foreign key (id_analista, chave_empresa)
            references tb_colaborador(id_colaborador, chave_empresa),
    constraint fk_empresa_denuncia
        foreign key (chave_empresa)
            references tb_empresa(chave_empresa),
    constraint fk_comentario_denuncia
        foreign key (id_comentario_item_evento, chave_empresa)
            references tb_comentario_item_evento(id_comentario_item_evento, chave_empresa)

);

create sequence seq_denuncia_comentario_item_evento start with 50 increment by 50;
