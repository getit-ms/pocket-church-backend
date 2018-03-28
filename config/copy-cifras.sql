insert into tb_arquivo(id_arquivo, nome, chave_igreja)
  select id_arquivo, nome, '<chave_igreja>' from tb_arquivo
  where id_arquivo in (select id_thumbnail from tb_cifra where chave_igreja = 'ipn') and chave_igreja = 'ipn';

insert into tb_arquivo(id_arquivo, nome, chave_igreja)
  select id_arquivo, nome, '<chave_igreja>' from tb_arquivo
  where id_arquivo in (select id_arquivo from tb_cifra where chave_igreja = 'ipn') and chave_igreja = 'ipn';

insert into tb_cifra(id_cifra, autor, letra, titulo, chave_igreja, id_arquivo, id_thumbnail, tipo)
  select id_cifra, autor, letra, titulo, '<chave_igreja>', id_arquivo, id_thumbnail, tipo from tb_cifra where chave_igreja = 'ipn';

insert into tb_arquivo(id_arquivo, nome, chave_igreja)
  select id_arquivo, nome, '<chave_igreja>' from tb_arquivo
  where id_arquivo in (select id_arquivo from rl_cifra_paginas where chave_igreja = 'ipn') and chave_igreja = 'ipn';

insert into rl_cifra_paginas(id_cifra, chave_igreja, id_arquivo)
  select id_cifra, '<chave_igreja>', id_arquivo from rl_cifra_paginas where chave_igreja = 'ipn';

