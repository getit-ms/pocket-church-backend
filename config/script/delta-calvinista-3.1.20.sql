alter table tb_notificacao add column data timestamp;
update tb_notificacao set data = now();
alter table tb_notificacao alter column data set not null;