
create view vw_aniversario_membro as
select 
	m.id_membro, m.chave_igreja,
	extract(day from m.data_nascimento) as dia,
	extract(month from m.data_nascimento) as mes,
	extract(day from m.data_nascimento) = extract(day from now() at time zone i.timezone) and
		extract(month from m.data_nascimento) = extract(month from now() at time zone i.timezone) as aniversariante
from tb_membro m inner join tb_igreja i on i.chave_igreja = m.chave_igreja;