alter table tb_evento add column status integer;
update tb_evento set status = 0;
alter table tb_evento alter column status set not null;