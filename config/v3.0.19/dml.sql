
insert into rl_plano_funcionalidade(id_plano, funcionalidade)
    select id_plano, 'ANALISA_DENUNCIAS_COMENTARIO' from tb_plano;

insert into rl_perfil_funcionalidade(id_perfil, chave_empresa, funcionalidade)
    select id_perfil, chave_empresa, 'ANALISA_DENUNCIAS_COMENTARIO' from tb_perfil where nome = 'Administrador';
