
-- NOTICIA
insert into tb_item_evento(id_item_evento, chave_empresa, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
    select trim(to_char(id_noticia, '9999999999999')), chave_empresa,
    case when tipo = 0 then 'NOTICIA' else 'CLASSIFICADOS' end, titulo, data_publicacao, 0,
    id_ilustracao, id_autor from tb_noticia;
