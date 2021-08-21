create table tb_item_evento
(
    id_item_evento varchar(150),
    chave_igreja   varchar(50),
    tipo           varchar(100),
    titulo         varchar(150),
    apresentacao   varchar(250),
    data_hora      timestamp,
    status         integer,
    id_ilustracao  bigint,
    id_autor       bigint,
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

create
index idx_item_evento
    on tb_item_evento(chave_igreja, tipo, id_item_evento);

create
index idx_item_evento_data
    on tb_item_evento(chave_igreja, status, data_hora);

update tb_audio
set data_hora = now();

-- AUDIO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
select trim(to_char(id_audio, '9999999999999')),
       chave_igreja,
       'AUDIO',
       nome,
       data_hora,
       0,
       id_arquivo_capa,
       id_autor
from tb_audio;

-- BOLETIM
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
select trim(to_char(id_boletim, '9999999999999')),
       chave_igreja,
       'BOLETIM',
       titulo,
       data_publicacao,
       case when status = 1 then 0 else 1 end,
       id_thumbnail,
       id_autor
from tb_boletim
where tipo = 'BOLETIM';

-- PUBLICACAO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
select trim(to_char(id_boletim, '9999999999999')),
       chave_igreja,
       'PUBLICACAO',
       titulo,
       data_publicacao,
       case when status = 1 then 0 else 1 end,
       id_thumbnail,
       id_autor
from tb_boletim
where tipo = 'PUBLICACAO';

-- DOCUMENTO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
select trim(to_char(id_documento, '9999999999999')),
       chave_igreja,
       'DOCUMENTO',
       titulo,
       data_publicacao,
       case when status = 1 then 0 else 1 end,
       id_thumbnail,
       id_membro
from tb_documento;

-- EVENTO_CALENDARIO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora, status)
select id,
       chave_igreja,
       'EVENTO_CALENDARIO',
       case when length(descricao) > 150 then substr(descricao, 150) else descricao end,
       data_inicio,
       0
from tb_evento_calendario;

alter table tb_item_evento
    add column url_ilustracao varchar(500);

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

alter table tb_item_evento alter column apresentacao type text;

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

-- NOTICIA
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora, status, apresentacao, id_ilustracao,
                           id_autor)
select trim(to_char(id_noticia, '9999999999999')),
       chave_igreja,
       'NOTICIA',
       titulo,
       data_publicacao,
       0,
       resumo,
       id_ilustracao,
       id_autor
from tb_noticia;

alter table tb_item_evento
    add column data_hora_referencia timestamp;

alter table tb_item_evento
    rename column data_hora to data_hora_publicacao;

create
index idx_item_evento_data_referencia
    on tb_item_evento(chave_igreja, status, data_hora_referencia);
update tb_item_evento
set data_hora_referencia = data_hora_publicacao;

delete
from tb_item_evento
where tipo = 'EVENTO_INSCRICAO';

-- EVENTO_INSCRICAO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao)
select trim(to_char(id_evento, '9999999999999')),
       chave_igreja,
       'EVENTO_INSCRICAO',
       nome,
       data_inicio_inscricao,
       data_hora_inicio,
       CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END,
       id_banner
from tb_evento
where tipo = 'EVENTO';

-- EBD
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao)
select trim(to_char(id_evento, '9999999999999')),
       chave_igreja,
       'EBD',
       nome,
       data_inicio_inscricao,
       data_hora_inicio,
       CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END,
       id_banner
from tb_evento
where tipo = 'EBD';

-- CULTO
insert into tb_item_evento(id_item_evento, chave_igreja, tipo, titulo, data_hora_publicacao, data_hora_referencia,
                           status, id_ilustracao)
select trim(to_char(id_evento, '9999999999999')),
       chave_igreja,
       'CULTO',
       nome,
       data_inicio_inscricao,
       data_hora_inicio,
       CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END,
       id_banner
from tb_evento
where tipo = 'CULTO';

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

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
select id_plano, 'ANALISA_DENUNCIAS_COMENTARIO'
from tb_plano;

insert into rl_perfil_funcionalidade(id_perfil, chave_igreja, funcionalidade)
select id_perfil, chave_igreja, 'ANALISA_DENUNCIAS_COMENTARIO'
from tb_perfil
where nome = 'Administrador';
