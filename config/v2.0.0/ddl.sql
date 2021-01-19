alter table tb_dispositivo add column ultimo_acesso timestamp;
alter table tb_empresa alter column nome_aplicativo type varchar(35);

create table tb_estatistica_acesso(
  chave_empresa varchar(150),
  data date,
  funcionalidade integer,
  quantidade_acessos_sucesso integer,
  quantidade_acessos_falhos integer,
  primary key (chave_empresa, funcionalidade, data)
);

create table tb_estatistica_dispositivo (
  chave_empresa varchar(50),
  data date,
  tipo_dispositivo integer,
  quantidade_dispositivos integer,
  quantidade_colaboradores_logados integer,
  primary key (chave_empresa, data, tipo_dispositivo)
);

create table tb_evento_calendario(
  id varchar(255),
  data_inicio timestamp,
  data_termino timestamp,
  descricao text,
  local text,
  ultima_alteracao timestamp,
  chave_empresa varchar(150),
  primary key (chave_empresa, id)
);

alter table tb_notificacao add column data timestamp;

create table tb_registro_acesso (
  data timestamp,
  funcionalidade integer,
  dispositivo varchar(255),
  chave_empresa varchar(150),
  status integer,
  primary key (chave_empresa, funcionalidade, dispositivo, data)
);

create table tb_template_empresa (
  chave_empresa varchar(150) primary key,
  cor_principal varchar(20),
  id_logo_pequena bigint,
  id_logo_grande bigint,
  id_banner bigint,
  id_logo_report bigint
);

create table tb_template_aplicativo (
  chave_empresa varchar(150) primary key,
  id_android_icon bigint,
  id_ios_icon bigint,
  id_splash bigint,
  id_push_icon bigint,
  id_background_home bigint,
  id_logo_home bigint,
  id_background_login bigint,
  id_logo_login bigint,
  id_background_menu bigint,
  id_logo_menu bigint,
  id_background_institucional bigint,
  id_logo_institucional bigint
);

create table tb_cores_aplicativo (
  chave_empresa varchar(150),
  chave varchar(255),
  cor varchar(20),
  primary key (chave_empresa, chave)
);

create index idx_estatistica_dispositivo on tb_dispositivo (chave_empresa, ultimo_acesso, tipo);

create index idx_empresa_colaborador_dispositivo on tb_dispositivo (chave_empresa, id_colaborador);

create index idx_push_dispositivo on tb_dispositivo (chave_empresa, pushkey);

create index idx_ultimo_acesso_dispositivo on tb_dispositivo (ultimo_acesso);