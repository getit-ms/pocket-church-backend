create table rl_endereco_igreja(
    chave_igreja varchar(20) not null,
    id_endereco bigint not null,
    primary key (chave_igreja, id_endereco)
);

insert into rl_endereco_igreja(chave_igreja, id_endereco) select chave_igreja, id_endereco from tb_institucional;

alter table tb_institucional drop column id_endereco;