create table tb_termo_aceite
(
    id_termo_aceite bigint      not null primary key,
    chave_igreja    varchar(50) not null,
    termo           text        not null,
    versao          integer     not null,
    constraint fk_igreja_termo_aceite
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja)
);

create sequence seq_termo_aceite start with 50 increment by 50;

create table rl_termo_aceite_membro
(
    id_membro         bigint       not null,
    id_termo_aceite   bigint       not null,
    chave_igreja      varchar(50)  not null,
    data_aceite       timestamp    not null,
    chave_dispositivo varchar(255) not null,
    primary key (id_membro, id_termo_aceite, chave_igreja),
    constraint fk_membro_termo_aceite_membro
        foreign key (id_membro, chave_igreja)
            references tb_membro (id_membro, chave_igreja),
    constraint fk_termo_aceite_termo_aceite_membro
        foreign key (id_termo_aceite, chave_igreja)
            references tb_termo_aceite (id_termo_aceite, chave_igreja),
    constraint fk_igreja_termo_aceite_membro
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja)
);
