insert into tb_igreja(chave_igreja, nome, status, id_plano, template, locale, timezone, nome_aplicativo, id_biblia)
	values('<chave_igreja>', '<nome_igreja>', 0, 1, 1, 'pt-br', 'America/Sao_Paulo', '<nome_aplicativo_igreja>', 1);
	
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

insert into tb_parametro(grupo, chave, valor) values('<chave_igreja>', 'GOOGLE_OAUTH_CLIENT_KEY', '65118528623-tlgg9invlav3nljorfrbd57uhrf9jiv2.apps.googleusercontent.com');
insert into tb_parametro(grupo, chave, valor) values('<chave_igreja>', 'GOOGLE_OAUTH_SECRET_KEY', '3QNbshcUP1gF_mk1RjafFtF3');
insert into tb_parametro(grupo, chave, valor) values('<chave_igreja>', 'PUSH_ANDROID_KEY', 'AIzaSyAACRTJbKBQe4Ufb3ZfN48abC_JCLhNU-Q');
insert into tb_parametro(grupo, chave, valor) values('<chave_igreja>', 'PUSH_ANDROID_SENDER_ID', 'AIzaSyAACRTJbKBQe4Ufb3ZfN48abC_JCLhNU-Q');
