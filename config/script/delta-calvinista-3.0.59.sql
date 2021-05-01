create table tb_opcao_leitura_biblica(
    id_opcao_leitura_biblica bigint not null,
    id_membro bigint not null,
    id_plano_leitura_biblica bigint not null,
    chave_igreja varchar(255) not null,
    inicio timestamp not null,
    termino timestamp,
    primary key (id_opcao_leitura_biblica)
);

create table tb_marcacao_leitura_biblica(
    id_marcacao_leitura_biblica bigint not null,
    chave_igreja varchar(255) not null,
    id_membro bigint not null,
    id_dia_leitura_biblica bigint not null,
    primary key (id_marcacao_leitura_biblica)
);


CREATE SEQUENCE seq_opcao_leitura_biblica
  INCREMENT 50
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 11500
  CACHE 1;

CREATE SEQUENCE seq_marcacao_leitura_biblica
  INCREMENT 50
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 11500
  CACHE 1;