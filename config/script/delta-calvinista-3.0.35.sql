alter table tb_hino add column ultima_alteracao timestamp;
update tb_hino set ultima_alteracao = now();
alter table tb_estudo add column ultima_alteracao timestamp;
update tb_estudo set ultima_alteracao = now();
alter table tb_evento add column ultima_alteracao timestamp;
update tb_evento set ultima_alteracao = now();