update tb_item_evento set data_hora_referencia = data_hora_publicacao;

delete from tb_item_evento where tipo = 'EVENTO_INSCRICAO';

-- EVENTO_INSCRICAO
insert into tb_item_evento(id_item_evento, chave_empresa, tipo, titulo, data_hora_publicacao, data_hora_referencia, status, id_ilustracao)
    select trim(to_char(id_evento, '9999999999999')), chave_empresa, 'EVENTO_INSCRICAO', nome, data_inicio_inscricao, data_hora_inicio,
    CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END, id_banner from tb_evento;

