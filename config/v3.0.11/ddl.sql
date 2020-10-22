alter table tb_item_evento
    add column data_hora_referencia timestamp;

alter table tb_item_evento
    rename column data_hora to data_hora_publicacao;

create index idx_item_evento_data_referencia
    on tb_item_evento(chave_empresa, status, data_hora_referencia);
