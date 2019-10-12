create table tb_token_firebase (
    id_token_firebase bigint primary key,
    versao varchar(20),
    token varchar(500) not null,
    chave_igreja varchar(50),
    constraint fk_igreja_token_firebase
        foreign key (chave_igreja)
        references tb_igreja(chave)
);

create sequence seq_token_firebase increment by 50 start with 50;