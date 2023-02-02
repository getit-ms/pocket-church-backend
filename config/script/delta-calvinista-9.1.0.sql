create table tb_banner
(
    id_banner          bigint,
    chave_igreja       varchar(50),
    id_arquivo         bigint,
    ordem              integer,
    link_externo       varchar(500),
    funcionalidade     varchar(100),
    referencia_interna varchar(150),
    primary key (id_banner, chave_igreja),
    constraint fk_arquivo_banner
        foreign key (id_arquivo, chave_igreja)
            references tb_arquivo (id_arquivo, chave_igreja),
    constraint fk_igreja_banner
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja)
);

create sequence seq_banner start with 50 increment by 50;

insert into tb_banner(id_banner, chave_igreja, id_arquivo, ordem)
select 1, chave_igreja, id_divulgacao, 1 from tb_institucional where id_divulgacao is not null;

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
select id_plano, 'MANTER_BANNERS' from tb_plano;

insert into rl_perfil_funcionalidade(id_perfil, chave_igreja, funcionalidade)
select id_perfil, chave_igreja, 'MANTER_BANNERS' from tb_perfil where nome = 'Administrador';
