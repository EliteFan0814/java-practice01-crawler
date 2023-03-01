ALTER DATABASE news CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
create table NEWS
(
    id          bigint primary key auto_increment,
    title       text,
    content     text,
    url         varchar(1000),
    created_at  timestamp default now(),
    modified_at timestamp default now()
);

create table LINKS_TO_BE_PROCESSED
(
    link varchar(1000)
);

create table LINKS_ALREADY_PROCESSED
(
    link varchar(1000)
);