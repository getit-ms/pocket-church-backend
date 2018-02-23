create sequence seq_menu START with 50 INCREMENT by 50;

create table tb_menu (
  id_menu bigint not null,
  chave_igreja varchar(255) not null,
  nome varchar(150) not null,
  icone varchar(255),
  ordem decimal not null,
  funcionalidade varchar(255),
  id_menu_pai bigint,
  primary key (id_menu, chave_igreja)
);

insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(1, 'tst', 'Início', 'fa fa-home', 1, 'INICIO_APLICATIVO', null);

insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(2, 'tst', 'Igreja', '', 2, null, null);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(3, 'tst', 'Institucional', null, 1, 'INSTITUCIONAL', 2);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(4, 'tst', 'Contatos', null, 2, 'CONSULTAR_CONTATOS_IGREJA', 2);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(5, 'tst', 'Eventos', null, 3, 'REALIZAR_INSCRICAO_EVENTO', 2);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(6, 'tst', 'Agenda da Igreja', null, 4, 'AGENDA', 2);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(7, 'tst', 'Aconselhamentos', null, 5, 'AGENDAR_ACONSELHAMENTO', 2);

insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(8, 'tst', 'Palavra', 'fa fa-book', 3, null, null);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(9, 'tst', 'Bíblia', null, 1, 'BIBLIA', 8);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(10, 'tst', 'EBD', null, 2, 'REALIZAR_INSCRICAO_EBD', 8);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(11, 'tst', 'Leitura Bíblica', null, 3, 'CONSULTAR_PLANOS_LEITURA_BIBLICA', 8);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(12, 'tst', 'Estudos', null, 4, 'LISTAR_ESTUDOS', 8);

insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(13, 'tst', 'Culto', 'fa fa-plus-square', 4, null, null);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(14, 'tst', 'Cultos e Vídeos', null, 1, 'YOUTUBE', 13);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(15, 'tst', 'Boletins', null, 2, 'LISTAR_BOLETINS', 13);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(16, 'tst', 'Publicações', null, 3, 'LISTAR_PUBLICACOES', 13);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(17, 'tst', 'Hinário', null, 4, 'CONSULTAR_HINARIO', 13);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(18, 'tst', 'Cânticos', null, 5, 'CONSULTAR_CIFRAS', 13);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(19, 'tst', 'Pedidos de Oração', null, 6, 'PEDIR_ORACAO', 13);

insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(20, 'tst', 'Utilidades', 'fa fa-ellipsis-h', 5, null, null);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(21, 'tst', 'Notificações', null, 1, 'NOTIFICACOES', 20);
insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(22, 'tst', 'Sugestões', null, 2, 'CHAMADOS', 20);

insert into tb_menu(id_menu, chave_igreja, nome, icone, ordem, funcionalidade, id_menu_pai) values(23, 'tst', 'Preferências', 'fa fa-cog', 6, 'PREFERENCIAS', null);