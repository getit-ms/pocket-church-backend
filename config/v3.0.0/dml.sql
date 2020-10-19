update tb_audio set data_hora = now();

-- AUDIO
insert into tb_item_evento(id_item_evento, chave_empresa, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
    select to_char(id_audio, '9999999999999'), chave_empresa, 'AUDIO', nome, data_hora, 0, id_arquivo_capa, id_autor from tb_audio;

-- BOLETIM_INFORMATIVO
insert into tb_item_evento(id_item_evento, chave_empresa, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
    select to_char(id_boletim, '9999999999999'), chave_empresa, 'BOLETIM_INFORMATIVO', titulo, data_publicacao, case when status = 1 then 0 else 1 end, id_thumbnail, id_autor from tb_boletim;

-- DOCUMENTO
insert into tb_item_evento(id_item_evento, chave_empresa, tipo, titulo, data_hora, status, id_ilustracao, id_autor)
    select to_char(id_documento, '9999999999999'), chave_empresa, 'DOCUMENTO', titulo, data_publicacao, case when status = 1 then 0 else 1 end, id_thumbnail, id_colaborador from tb_documento;

-- EVENTO_INSCRICAO
insert into tb_item_evento(id_item_evento, chave_empresa, tipo, titulo, data_hora, status, id_ilustracao)
    select to_char(id_evento, '9999999999999'), chave_empresa, 'EVENTO_INSCRICAO', nome, data_hora_inicio,
    CASE WHEN data_hora_termino < current_timestamp THEN 0 ELSE 1 END, id_banner from tb_evento;

-- EVENTO_CALENDARIO
insert into tb_item_evento(id_item_evento, chave_empresa, tipo, titulo, data_hora, status)
    select id, chave_empresa, 'EVENTO_CALENDARIO',
    case when length(descricao) > 150 then substr(descricao, 150) else descricao end,
    data_inicio, 0 from tb_evento_calendario;
