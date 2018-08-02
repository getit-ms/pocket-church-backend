insert into tb_empresa(chave_empresa, nome, status, id_plano, locale, timezone, nome_aplicativo)
	values('<chave_empresa>', '<nome_empresa>', 0, 1, 'pt-br', 'America/Sao_Paulo', '<nome_aplicativo_empresa>');
	
insert into tb_endereco(id_endereco) values(nextval('seq_endereco'));

insert into tb_colaborador(id_colaborador, email, nome, senha, status, chave_empresa, gerente, id_endereco, visitante, dados_disponiveis, deseja_disponibilizar_dados)	values
	(nextval('seq_colaborador'), 'getitmobilesolutions@gmail.com', 'GET IT Mobile Solutions', '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 0, '<chave_empresa>', 'f', (select max(id_endereco) from tb_endereco), true, true, true);

insert into tb_acesso(id_colaborador, chave_empresa) values((select max(id_colaborador) from tb_colaborador), '<chave_empresa>');

insert into tb_perfil(id_perfil, nome, chave_empresa)
	values(nextval('seq_perfil'), 'Administrador', '<chave_empresa>');

insert into rl_acesso_perfil(id_colaborador, chave_empresa, id_perfil)
	values((select max(id_colaborador) from tb_colaborador), '<chave_empresa>', (select max(id_perfil) from tb_perfil));
	
insert into rl_perfil_funcionalidade (id_perfil, chave_empresa, funcionalidade)
	select (select max(id_perfil) from tb_perfil), '<chave_empresa>', funcionalidade from rl_perfil_funcionalidade where id_perfil = 1;

insert into tb_parametro(grupo, chave, valor) values('<chave_empresa>', 'GOOGLE_OAUTH_CLIENT_KEY', '65118528623-tlgg9invlav3nljorfrbd57uhrf9jiv2.apps.googleusercontent.com');
insert into tb_parametro(grupo, chave, valor) values('<chave_empresa>', 'GOOGLE_OAUTH_SECRET_KEY', '3QNbshcUP1gF_mk1RjafFtF3');
insert into tb_parametro(grupo, chave, valor) values('<chave_empresa>', 'PUSH_ANDROID_KEY', 'AIzaSyAACRTJbKBQe4Ufb3ZfN48abC_JCLhNU-Q');
insert into tb_parametro(grupo, chave, valor) values('<chave_empresa>', 'PUSH_ANDROID_SENDER_ID', 'AIzaSyAACRTJbKBQe4Ufb3ZfN48abC_JCLhNU-Q');
