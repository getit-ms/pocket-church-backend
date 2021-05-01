alter table tb_plano_leitura_biblica add column ultima_alteracao timestamp;
update tb_plano_leitura_biblica set ultima_alteracao = now();

alter table tb_marcacao_leitura_biblica add column data timestamp;

alter table tb_preferencias add column deseja_receber_lembretes_leitura_biblica boolean;
update tb_preferencias set deseja_receber_lembretes_leitura_biblica = true;

alter table tb_preferencias add column hora_lembrete_leitura integer;
update tb_preferencias set hora_lembrete_leitura = 1;