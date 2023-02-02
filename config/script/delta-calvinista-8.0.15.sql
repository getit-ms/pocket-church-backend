create table tb_campo_evento (
    id_campo_evento bigint primary key,
    nome varchar(150),
    tipo integer,
    formato integer,
    id_evento bigint,
    chave_igreja varchar(50),
    constraint fk_evento_campo_evento
        foreign key (id_evento, chave_igreja)
            references tb_evento(id_evento, chave_igreja),
    constraint fk_igreja_campo_evento
        foreign key (chave_igreja)
            references tb_igreja(chave_igreja)
);

create sequence seq_campo_evento increment by 50 start with 50;

create table tb_opcoes_campo_evento(
    id_campo_evento bigint,
    opcao varchar(150),
    primary key (id_campo_evento, opcao),
    constraint fk_campo_evento_opcao
        foreign key (id_campo_evento)
            references tb_campo_evento(id_campo_evento)
);

create table tb_validacao_campo_evento(
    id_campo_evento bigint,
    tipo integer,
    valor varchar(150),
    primary key (id_campo_evento, tipo),
    constraint fk_campo_evento_validacao
        foreign key (id_campo_evento)
            references tb_campo_evento(id_campo_evento)
);

create table tb_valores_inscricao_evento(
    id_inscricao bigint,
    id_evento bigint,
    chave_igreja varchar(50),
    nome varchar (150),
    valor varchar(150),
    primary key (id_inscricao, id_evento, chave_igreja, nome),
    constraint fk_valor_inscricao_evento
        foreign key (id_inscricao, id_evento)
            references tb_inscricao_evento(id_inscricao, id_evento)
);

alter table tb_inscricao_evento add column chave_dispositivo varchar(255);
