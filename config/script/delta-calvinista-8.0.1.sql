alter table tb_preferencias add column deseja_receber_notificacoes_devocionario boolean;

update tb_preferencias set deseja_receber_notificacoes_devocionario = true;

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
    select id_plano, 'MANTEM_DEVOCIONARIO' from tb_plano where id_plano in (3,1,2,4,5,6,7,8952);

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
    select id_plano, 'DEVOCIONARIO' from tb_plano where id_plano in (3,1,2,4,5,6,7,8952);

insert into rl_perfil_funcionalidade(id_perfil, chave_igreja, funcionalidade)
    select id_perfil, chave_igreja, 'MANTEM_DEVOCIONARIO' from tb_perfil
    where nome = 'Administrador' and chave_igreja in (select chave_igreja from tb_igreja where id_plano in (3,1,2,4,5,6,7,8952));

insert into rl_igreja_funcionalidade_aplicativo(chave_igreja, funcionalidade)
    select chave_igreja, 'DEVOCIONARIO' from tb_igreja where id_plano in (3,1,2,4,5,6,7,8952);

insert into tb_menu(id_menu, chave_igreja, nome, icone, link, ordem, funcionalidade, id_menu_pai)
    select nextval('seq_menu'), mnu.chave_igreja, 'Devocionário', mnu.icone, 'devocionario',
    (select max(othr.ordem) + 1 from tb_menu othr where othr.id_menu_pai = mnu.id_menu_pai), 'DEVOCIONARIO', mnu.id_menu_pai
        from tb_menu mnu where funcionalidade = 'LISTAR_ESTUDOS'
        and chave_igreja in (select chave_igreja from tb_igreja where id_plano in (3,1,2,4,5,6,7,8952));

