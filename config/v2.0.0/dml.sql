update tb_dispositivo set ultimo_acesso = now();

update tb_parametro set chave = 'PUSH_TITLE_MENSAGEM_DIA' where chave = 'TITULO_MENSAGEM_DIA';
update tb_parametro set chave = 'PUSH_TITLE_ANIVERSARIO' where chave = 'TITULO_ANIVERSARIO';
update tb_parametro set chave = 'PUSH_BODY_ANIVERSARIO' where chave = 'TEXTO_ANIVERSARIO';
update tb_parametro set chave = 'PUSH_TITLE_PUBLICACAO' where chave = 'TITULO_PUBLICACAO';
update tb_parametro set chave = 'PUSH_BODY_PUBLICACAO' where chave = 'TEXTO_PUBLICACAO';
update tb_parametro set chave = 'PUSH_TITLE_BOLETIM' where chave = 'TITULO_BOLETIM';
update tb_parametro set chave = 'PUSH_BODY_BOLETIM' where chave = 'TEXTO_BOLETIM';
update tb_parametro set chave = 'PUSH_TITLE_DOCUMENTO' where chave = 'TITULO_DOCUMENTO';
update tb_parametro set chave = 'PUSH_BODY_DOCUMENTO' where chave = 'TEXTO_DOCUMENTO';
update tb_parametro set chave = 'PUSH_TITLE_NOTICIA' where chave = 'TITULO_NOTICIA';
update tb_parametro set chave = 'PUSH_BODY_NOTICIA' where chave = 'TEXTO_NOTICIA';
update tb_parametro set chave = 'PUSH_TITLE_CLASSIFICADOS' where chave = 'TITULO_CLASSIFICADOS';
update tb_parametro set chave = 'PUSH_BODY_CLASSIFICADOS' where chave = 'TEXTO_CLASSIFICADOS';
update tb_parametro set chave = 'PUSH_TITLE_YOUTUBE_AO_VIVO' where chave = 'TITULO_YOUTUBE_AO_VIVO';
update tb_parametro set chave = 'PUSH_BODY_YOUTUBE_AO_VIVO' where chave = 'TEXTO_YOUTUBE_AO_VIVO';
update tb_parametro set chave = 'PUSH_TITLE_YOUTUBE_AGENDADO' where chave = 'TITULO_YOUTUBE_AGENDADO';
update tb_parametro set chave = 'PUSH_BODY_YOUTUBE_AGENDADO' where chave = 'TEXTO_YOUTUBE_AGENDADO';