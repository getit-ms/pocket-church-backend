alter table tb_livro_biblia rename to tb_livro_biblia_old;
alter table tb_versiculo_biblia rename to tb_versiculo_biblia_old;

create table tb_livro_biblia(
  id_livro_biblia bigint not null,
  id_biblia bigint not null,
  nome varchar(50),
  ordem integer,
  abreviacao varchar(5),
  ultima_atualizacao timestamp with TIME ZONE,
  testamento integer,
  constraint tb_livro_biblia_pkey
  primary key(id_livro_biblia, id_biblia)
);

create table tb_versiculo_biblia
(
  id_versiculo_biblia bigint not null
    constraint tb_versiculo_biblia_pkey
    primary key,
  capitulo integer not null,
  versiculo integer not null,
  texto text not null,
  id_livro_biblia bigint not null,
  id_biblia bigint not null
);

insert into tb_livro_biblia(id_livro_biblia, id_biblia, nome, ordem, abreviacao, ultima_atualizacao, testamento)
    select id_livro_biblia, id_biblia, nome, ordem, abreviacao, ultima_atualizacao, testamento from tb_livro_biblia_old;

insert into tb_versiculo_biblia(id_versiculo_biblia, capitulo, versiculo, texto, id_livro_biblia, id_biblia)
    select v.id_versiculo_biblia, v.capitulo, v.versiculo, v.texto, v.id_livro_biblia, l.id_biblia
      from tb_versiculo_biblia_old v inner join tb_livro_biblia_old l on v.id_livro_biblia = l.id_livro_biblia;

update tb_livro_biblia set ultima_atualizacao = now();

