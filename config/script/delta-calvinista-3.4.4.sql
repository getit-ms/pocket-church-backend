-- DDL

alter table tb_membro add column id_foto bigint;

alter table tb_estudo add column divulgado boolean;
alter table tb_estudo alter column texto drop not null;
alter table tb_estudo add column tipo decimal(1);

alter table tb_estudo add column id_pdf bigint
alter table tb_estudo add column id_thumbnail bigint

create table rl_estudo_paginas(
chave_igreja varchar(255) not null,
id_estudo bigint not null,
id_arquivo bigint not null,
primary key(chave_igreja, id_estudo, id_arquivo)
);

create table tb_categoria_estudo(
  id_categoria_estudo bigint,
  nome varchar(150) not NULL,
  chave_igreja varchar(255) not null,
  primary key (id_categoria_estudo, chave_igreja)
);

create sequence seq_categoria_estudo;

alter table tb_estudo add column id_categoria bigint;

-- DML

update tb_estudo set tipo = 0;
update tb_estudo set divulgado = (status = 1);
update tb_estudo set status = 1;

insert into rl_plano_funcionalidade(id_plano, funcionalidade) VALUES (1, 'MANTER_PUBLICACOES');

insert into tb_categoria_estudo(id_categoria_estudo, nome, chave_igreja) select 1, 'Estudos Anteriores', chave_igreja from tb_igreja;

select nextval('seq_categoria_estudo');

update tb_estudo set id_categoria = 1;