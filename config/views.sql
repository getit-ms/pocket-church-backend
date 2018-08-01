
create view vw_aniversario_colaborador as
select
	m.id_colaborador, m.chave_empresa,
	extract(day from m.data_nascimento) as dia,
	extract(month from m.data_nascimento) as mes,
	extract(day from m.data_nascimento) = extract(day from now() at time zone i.timezone) and
		extract(month from m.data_nascimento) = extract(month from now() at time zone i.timezone) as aniversariante
from tb_colaborador m inner join tb_empresa i on i.chave_empresa = m.chave_empresa;