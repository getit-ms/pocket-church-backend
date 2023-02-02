create table tb_plano_leitura_biblica(
    id_plano_leitura_biblica bigint not null,
    chave_igreja varchar(255) not null,
    descricao varchar(150) not null,
    primary key (id_plano_leitura_biblica, chave_igreja)
);

create table tb_dia_leitura_biblica(
    id_dia_leitura_biblica bigint not null,
    chave_igreja varchar(255) not null,
    data timestamp with time zone,
    descricao varchar(150),
    id_plano_leitura_biblica bigint not null,
    primary key (id_dia_leitura_biblica)
);


CREATE SEQUENCE seq_plano_leitura_biblica
  INCREMENT 50
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 11500
  CACHE 1;

CREATE SEQUENCE seq_dia_leitura_biblica
  INCREMENT 50
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 11500
  CACHE 1;

insert into rl_plano_funcionalidade(id_plano, funcionalidade) values(1, 'MANTER_PLANOS_LEITURA_BIBLICA');
insert into rl_plano_funcionalidade(id_plano, funcionalidade) values(1, 'CONSULTAR_PLANOS_LEITURA_BIBLICA');
insert into rl_perfil_funcionalidade(chave_igreja, id_perfil, funcionalidade) select chave_igreja, id_perfil, 'MANTER_PLANOS_LEITURA_BIBLICA' from tb_perfil where chave_igreja = 'tst';
insert into rl_igreja_funcionalidade_aplicativo(chave_igreja, funcionalidade) values ('tst', 'CONSULTAR_PLANOS_LEITURA_BIBLICA');
commit;