alter table tb_item_evento add column url_ilustracao varchar(500);

create table tb_video (
    id_video varchar(250),
    chave_empresa varchar(50),
    titulo varchar(250),
    descricao text,
    thumbnail varchar(500),
    data_publicacao timestamp,
    data_agendamento timestamp,
    data_atualizacao timestamp,
    ao_vivo boolean,
    primary key (id_video, chave_empresa),
    constraint fk_empresa_video
        foreign key (chave_empresa)
            references tb_empresa(chave_empresa)
);
