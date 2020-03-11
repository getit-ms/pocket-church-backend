alter table tb_cifra add column ultima_alteracao timestamp;
update tb_cifra set ultima_alteracao = now();

alter table tb_dia_devocionario add column agendado boolean;
update tb_dia_devocionario set agendado = false;
