create table tb_dia_devocionario (
    id_dia_devocionario bigint,
    chave_igreja varchar(255),
    data date,
    divulgado boolean,
    status integer,
    data_alteracao timestamp,
    id_arquivo bigint,
    id_thumbnail bigint,
    primary key (id_dia_devocionario, chave_igreja),
    constraint fk_arquivo_dia_devocional
        foreign key (id_arquivo, chave_igreja)
            references tb_arquivo(id_arquivo, chave_igreja),
    constraint fk_thumbnail_dia_devocional
        foreign key (id_thumbnail, chave_igreja)
            references tb_arquivo(id_arquivo, chave_igreja)
);

create sequence seq_dia_devocionario start with 50 increment by 50;
