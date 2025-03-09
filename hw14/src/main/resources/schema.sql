create table authors (
    id bigserial,
    full_name varchar(255),
    primary key (id)
);

create table genres (
    id bigserial,
    name varchar(255),
    primary key (id)
);

create table books (
    id bigserial,
    title varchar(255),
    author_id bigint references authors (id) on delete cascade,
    primary key (id)
);

create table books_genres (
    book_id bigint references books(id) on delete cascade,
    genre_id bigint references genres(id) on delete cascade,
    primary key (book_id, genre_id)
);

create table book_comments(
    id bigserial,
    book_id bigint references books(id) on delete cascade,
    comment varchar(255)
);

create table item_mapping(
      h2_id bigserial,
      mongo_id varchar(255),
      item_type varchar(10)
);

create unique index idx_mapping ON item_mapping(mongo_id, item_type);

create sequence authors_seq;

create sequence genres_seq;

create sequence books_seq;

create sequence book_comments_seq;