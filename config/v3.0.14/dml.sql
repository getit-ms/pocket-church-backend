insert into tb_banner(id_banner, chave_empresa, id_arquivo, ordem)
    select 1, chave_empresa, id_divulgacao, 1 from tb_institucional where id_divulgacao is not null;

insert into rl_plano_funcionalidade(id_plano, funcionalidade)
    select id_plano, 'MANTER_BANNERS' from tb_plano;

insert into rl_perfil_funcionalidade(id_perfil, chave_empresa, funcionalidade)
    select id_perfil, chave_empresa, 'MANTER_BANNERS' from tb_perfil where nome = 'Administrador';
