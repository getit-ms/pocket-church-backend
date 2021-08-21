create table tb_termo_aceite
(
    id_termo_aceite bigint      not null,
    chave_igreja    varchar(50) not null,
    termo           text        not null,
    versao          integer     not null,
    primary key (id_termo_aceite, chave_igreja),
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

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
select id_plano, 'MANTER_TERMO_ACEITE' from tb_plano where id_plano in (3,1,2,4,5,6,7,8952);

insert into rl_perfil_funcionalidade(id_perfil, chave_igreja, funcionalidade)
select id_perfil, chave_igreja, 'MANTER_TERMO_ACEITE' from tb_perfil
where nome = 'Administrador' and chave_igreja in (select chave_igreja from tb_igreja where id_plano in (3,1,2,4,5,6,7,8952));
