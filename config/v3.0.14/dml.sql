insert into tb_banner(id_banner, chave_empresa, id_banner)
    select 1, chave_empresa, id_divulgacao from tb_institucional where id_divulgacao is not null;

insert into rl_perfil_funcionalidade(id_perfil, chave_empresa, funcionalidade)
    select id_perfil, chave_empresa, 'MANTER_BANNERS' from tb_perfil where nome = 'Administrador';
