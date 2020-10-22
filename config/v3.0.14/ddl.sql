create table tb_banner (
    id_banner bigint,
    chave_empresa varchar(50),
    id_arquivo bigint,
    ordem integer,
    link_externo varchar(500),
    funcionalidade varchar(100),
    referencia_interna varchar(150),
    primary key (id_banner, chave_empresa),
    constraint fk_arquivo_banner
        foreign key (id_arquivo, chave_empresa)
            references tb_arquivo(id_arquivo, chave_empresa),
    constraint fk_empresa_banner
        foreign key (chave_empresa)
            references tb_empresa(chave_empresa)
);

create sequence seq_banner start with 50 increment by 50;

alter table tb_resposta_questao drop column branco;
alter table tb_resposta_questao drop column nulo;
alter table tb_questao drop column quantidade_votos;
