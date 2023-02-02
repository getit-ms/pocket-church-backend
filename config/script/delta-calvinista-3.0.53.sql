alter table tb_preferencias add column deseja_receber_notificacoes_videos boolean;
update tb_preferencias set deseja_receber_notificacoes_videos = true;
alter table tb_preferencias alter column deseja_receber_notificacoes_videos set not null;