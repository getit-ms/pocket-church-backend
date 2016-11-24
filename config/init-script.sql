insert into tb_igreja(chave_igreja, nome, status, id_plano, template, locale, timezone, nome_aplicativo)
	values('<chave_igreja>', '<nome_igreja>', 0, 1, 1, 'pt-br', 'America/Sao_Paulo', '<nome_aplicativo_igreja>');
	
insert into rl_igreja_funcionalidade_aplicativo values('<chave_igreja>', 'REALIZAR_INSCRICAO_EVENTO');
insert into rl_igreja_funcionalidade_aplicativo values('<chave_igreja>', 'AGENDAR_ACONSELHAMENTO');
insert into rl_igreja_funcionalidade_aplicativo values('<chave_igreja>', 'PEDIR_ORACAO');
insert into rl_igreja_funcionalidade_aplicativo values('<chave_igreja>', 'CONSULTAR_CONTATOS_IGREJA');
	
insert into tb_endereco(id_endereco) values(nextval('seq_endereco'));

insert into tb_membro(id_membro, email, nome, senha, status, chave_igreja, pastor, id_endereco, visitante)	values
	(nextval('seq_membro'), 'getitmobilesolutions@gmail.com', 'Administrador', '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 0, '<chave_igreja>', 'f', (select max(id_endereco) from tb_endereco), true);

insert into tb_acesso(id_membro, chave_igreja) values((select max(id_membro) from tb_membro), '<chave_igreja>');

insert into tb_perfil(id_perfil, nome, chave_igreja)
	values(nextval('seq_perfil'), 'Administrador', '<chave_igreja>');

insert into rl_acesso_perfil(id_membro, chave_igreja, id_perfil)
	values((select max(id_membro) from tb_membro), '<chave_igreja>', (select max(id_perfil) from tb_perfil));
	
insert into rl_perfil_funcionalidade (id_perfil, chave_igreja, funcionalidade)
	select (select max(id_perfil) from tb_perfil), '<chave_igreja>', funcionalidade from rl_perfil_funcionalidade where id_perfil = 1;


