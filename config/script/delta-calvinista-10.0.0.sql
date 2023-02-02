create table tb_item_evento
(
    id_item_evento       varchar(150),
    chave_igreja         varchar(50),
    url_ilustracao       varchar(500),
    tipo                 varchar(100),
    titulo               varchar(150),
    apresentacao         text,
    data_hora_publicacao timestamp,
    data_hora_referencia timestamp,
    status               integer,
    id_ilustracao        bigint,
    id_autor             bigint,
    primary key (id_item_evento, chave_igreja, tipo),
    constraint fk_igreja_item_evento
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja),
    constraint fk_ilustracao_item_evento
        foreign key (chave_igreja, id_ilustracao)
            references tb_arquivo (chave_igreja, id_arquivo),
    constraint fk_autor_item_evento
        foreign key (chave_igreja, id_autor)
            references tb_membro (chave_igreja, id_membro)
);

create index idx_item_evento_data_referencia
    on tb_item_evento (chave_igreja, status, data_hora_referencia);

alter table tb_boletim
    add column id_autor bigint;

alter table tb_boletim
    add constraint fk_autor_boletim_informativo
        foreign key (chave_igreja, id_autor)
            references tb_membro (chave_igreja, id_membro);

alter table tb_documento
    rename column autor to autoria;

alter table tb_audio
    add column data_hora timestamp;

alter table tb_audio
    rename column autor to autoria;

alter table tb_audio
    add column id_autor bigint;

alter table tb_audio
    add constraint fk_autor_audio
        foreign key (chave_igreja, id_autor)
            references tb_membro (chave_igreja, id_membro);

create index idx_item_evento
    on tb_item_evento (chave_igreja, tipo, id_item_evento);

create index idx_item_evento_data
    on tb_item_evento (chave_igreja, status, data_hora_publicacao);

create table tb_video
(
    id_video         varchar(250),
    chave_igreja     varchar(50),
    titulo           varchar(250),
    descricao        text,
    thumbnail        varchar(500),
    data_publicacao  timestamp,
    data_agendamento timestamp,
    data_atualizacao timestamp,
    ao_vivo          boolean,
    primary key (id_video, chave_igreja),
    constraint fk_igreja_video
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja)
);

create table tb_galeria_fotos
(
    id_galeria_fotos   varchar(150),
    chave_igreja       varchar(50),
    nome               varchar(150),
    descricao          text,
    data_atualizacao   timestamp,
    data_sincronizacao timestamp,
    foto_primaria      text,
    quantidade_fotos   integer,
    primary key (id_galeria_fotos, chave_igreja),
    constraint fk_igreja_galeria_fotos
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja)
);

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

create table tb_curtida_item_evento
(
    id_membro        bigint,
    id_item_evento   varchar(150),
    chave_igreja     varchar(50),
    tipo_item_evento varchar(50),
    data_hora        timestamp,
    primary key (id_membro, id_item_evento, chave_igreja, tipo_item_evento),
    constraint fk_membro_curtida
        foreign key (id_membro, chave_igreja)
            references tb_membro (id_membro, chave_igreja),
    constraint fk_igreja_curtida
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja)
);

create table tb_comentario_item_evento
(
    id_comentario_item_evento bigint,
    id_membro                 bigint,
    id_item_evento            varchar(150),
    chave_igreja              varchar(50),
    tipo_item_evento          varchar(50),
    data_hora                 timestamp,
    comentario                text,
    status                    integer,
    primary key (id_comentario_item_evento, chave_igreja),
    constraint fk_membro_comentario
        foreign key (id_membro, chave_igreja)
            references tb_membro (id_membro, chave_igreja),
    constraint fk_igreja_comentario
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja)
);

create sequence seq_comentario_item_evento increment by 50 start with 50;

create table tb_denuncia_comentario_item_evento
(
    id_denuncia_comentario_item_evento bigint,
    id_denunciante                     bigint,
    id_analista                        bigint,
    id_comentario_item_evento          bigint,
    chave_igreja                       varchar(50),
    data_hora_denuncia                 timestamp,
    data_hora_analise                  timestamp,
    justificativa                      text,
    status                             integer,
    primary key (id_denuncia_comentario_item_evento, chave_igreja),
    constraint fk_denunciante_denuncia
        foreign key (id_denunciante, chave_igreja)
            references tb_membro (id_membro, chave_igreja),
    constraint fk_analista_denuncia
        foreign key (id_analista, chave_igreja)
            references tb_membro (id_membro, chave_igreja),
    constraint fk_igreja_denuncia
        foreign key (chave_igreja)
            references tb_igreja (chave_igreja),
    constraint fk_comentario_denuncia
        foreign key (id_comentario_item_evento, chave_igreja)
            references tb_comentario_item_evento (id_comentario_item_evento, chave_igreja)

);

create sequence seq_denuncia_comentario_item_evento start with 50 increment by 50;

update tb_audio
set data_hora = data_cadastro;

-- AUDIO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao, id_autor)
select trim(to_char(id_audio, '9999999999999')),
       chave_igreja,
       'AUDIO',
       nome,
       data_hora,
       data_hora,
       0,
       id_arquivo_capa,
       id_autor
from tb_audio;

-- BOLETIM
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao, id_autor)
select trim(to_char(id_boletim, '9999999999999')),
       chave_igreja,
       'BOLETIM',
       titulo,
       data_publicacao,
       data_publicacao,
       case when status = 1 then 0 else 1 end,
       id_thumbnail,
       id_autor
from tb_boletim
where tipo = 0;

-- PUBLICACAO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao, id_autor)
select trim(to_char(id_boletim, '9999999999999')),
       chave_igreja,
       'PUBLICACAO',
       titulo,
       data_publicacao,
       data_publicacao,
       case when status = 1 then 0 else 1 end,
       id_thumbnail,
       id_autor
from tb_boletim
where tipo = 1;

-- DOCUMENTO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao, id_autor)
select trim(to_char(id_estudo, '9999999999999')),
       chave_igreja,
       'ESTUDO',
       titulo,
       data_publicacao,
       data_publicacao,
       case when status = 1 then 0 else 1 end,
       id_thumbnail,
       id_membro
from tb_estudo;

-- EVENTO_CALENDARIO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status)
select id,
       chave_igreja,
       'EVENTO_CALENDARIO',
       case when length(descricao) > 150 then substr(descricao, 150) else descricao end,
       data_inicio,
       data_inicio,
       0
from tb_evento_calendario;


insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, apresentacao, id_ilustracao,
                           id_autor)
select trim(to_char(id_noticia, '9999999999999')),
       chave_igreja,
       'NOTICIA',
       titulo,
       data_publicacao,
       data_publicacao,
       0,
       resumo,
       id_ilustracao,
       id_autor
from tb_noticia;

-- EVENTO_INSCRICAO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao)
select trim(to_char(id_evento, '9999999999999')),
       chave_igreja,
       'EVENTO_INSCRICAO',
       case when length(nome) > 150 then substr(nome, 150) else nome end,
       data_inicio_inscricao,
       data_hora_inicio,
       CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END,
       id_banner
from tb_evento
where tipo = 0;

-- EBD
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao)
select trim(to_char(id_evento, '9999999999999')),
       chave_igreja,
       'EBD',
       case when length(nome) > 150 then substr(nome, 150) else nome end,
       data_inicio_inscricao,
       data_hora_inicio,
       CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END,
       id_banner
from tb_evento
where tipo = 1;

-- CULTO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao)
select trim(to_char(id_evento, '9999999999999')),
       chave_igreja,
       'CULTO',
       case when length(nome) > 150 then substr(nome, 150) else nome end,
       data_inicio_inscricao,
       data_hora_inicio,
       CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END,
       id_banner
from tb_evento
where tipo = 2;

insert into tb_banner(id_banner, chave_igreja, id_arquivo, ordem)
select 1, chave_igreja, id_divulgacao, 1
from tb_institucional
where id_divulgacao is not null;

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
select id_plano, 'MANTER_BANNERS'
from tb_plano;

insert into rl_perfil_funcionalidade(id_perfil, chave_igreja, funcionalidade)
select id_perfil, chave_igreja, 'MANTER_BANNERS'
from tb_perfil
where nome = 'Administrador';

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
select id_plano, 'ANALISA_DENUNCIAS_COMENTARIO'
from tb_plano;

insert into rl_perfil_funcionalidade(id_perfil, chave_igreja, funcionalidade)
select id_perfil, chave_igreja, 'ANALISA_DENUNCIAS_COMENTARIO'
from tb_perfil
where nome = 'Administrador';
