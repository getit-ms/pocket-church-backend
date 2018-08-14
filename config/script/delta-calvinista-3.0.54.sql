alter table tb_preferencias add column deseja_receber_notificacoes_videos boolean;
update tb_preferencias set deseja_receber_notificacoes_videos = true;
alter table tb_preferencias alter column deseja_receber_notificacoes_videos set not null;

alter table tb_horario_atendimento alter column hora_inicio type time without time zone;
alter table tb_horario_atendimento alter column hora_fim type time without time zone;

alter table tb_estudo add column nova_data date;
update tb_estudo b set nova_data = (select (bb.data::timestamp with time zone at time zone 'GMT-1:00')::date 
		from tb_estudo bb inner join tb_igreja i on i.chave_igreja = bb.chave_igreja where b.id_estudo = bb.id_estudo and b.chave_igreja = i.chave_igreja);
alter table tb_estudo drop column data;
alter table tb_estudo rename column nova_data to data;

alter table tb_horario_atendimento add column nova_data_inicio date;
update tb_horario_atendimento b set nova_data_inicio = (select (bb.data_inicio::timestamp with time zone at time zone 'GMT-1:00')::date 
		from tb_horario_atendimento bb inner join tb_igreja i on i.chave_igreja = bb.chave_igreja where b.id_horario_atendimento = bb.id_horario_atendimento and b.chave_igreja = i.chave_igreja);
alter table tb_horario_atendimento drop column data_inicio;
alter table tb_horario_atendimento rename column nova_data_inicio to data_inicio;

alter table tb_horario_atendimento add column nova_data_fim date;
update tb_horario_atendimento b set nova_data_fim = (select (bb.data_fim::timestamp with time zone at time zone 'GMT-1:00')::date 
		from tb_horario_atendimento bb inner join tb_igreja i on i.chave_igreja = bb.chave_igreja where b.id_horario_atendimento = bb.id_horario_atendimento and b.chave_igreja = i.chave_igreja);
alter table tb_horario_atendimento drop column data_fim;
alter table tb_horario_atendimento rename column nova_data_fim to data_fim;

alter table tb_membro add column nova_data_nascimento date;
update tb_membro b set nova_data_nascimento = (select (bb.data_nascimento::timestamp with time zone at time zone 'GMT-1:00')::date 
		from tb_membro bb inner join tb_igreja i on i.chave_igreja = bb.chave_igreja where b.id_membro = bb.id_membro and b.chave_igreja = i.chave_igreja)
		where extract(hour from data_nascimento) <> 0;
update tb_membro b set nova_data_nascimento = data_nascimento::date where nova_data_nascimento is null and data_nascimento is not null;
drop view vw_aniversario_membro;
alter table tb_membro drop column data_nascimento;
alter table tb_membro rename column nova_data_nascimento to data_nascimento;
create view vw_aniversario_membro as
select 
	m.id_membro, m.chave_igreja,
	extract(day from m.data_nascimento) as dia,
	extract(month from m.data_nascimento) as mes,
	extract(day from m.data_nascimento) = extract(day from now() at time zone i.timezone) and
		extract(month from m.data_nascimento) = extract(month from now() at time zone i.timezone) as aniversariante
from tb_membro m inner join tb_igreja i on i.chave_igreja = m.chave_igreja;

alter table tb_dia_leitura_biblica alter column data type date;
