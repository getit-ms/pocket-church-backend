alter table tb_membro add column id_foto bigint;

alter table tb_estudo add column divulgado boolean;
alter table tb_estudo alter column texto drop not null;
alter table tb_estudo add column tipo decimal(1);
update tb_estudo set tipo = 0;
update tb_estudo set divulgado = (status = 1);
update tb_estudo set status = 1;

alter table tb_estudo add column id_pdf bigint
alter table tb_estudo add column id_thumbnail bigint

create table rl_estudo_paginas(
chave_igreja varchar(255) not null,
id_estudo bigint not null,
id_arquivo bigint not null,
primary key(chave_igreja, id_estudo, id_arquivo)
);
