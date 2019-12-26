alter table tb_preferencias add column hora_notificacoes_devocionario integer;
update tb_preferencias set hora_notificacoes_devocionario = 1;

alter table tb_dia_devocionario drop column divulgado;
