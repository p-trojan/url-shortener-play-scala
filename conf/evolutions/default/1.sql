# --- !Ups

create table "urls" (
  "id" bigint generated by default as identity(start with 1) not null primary key,
  "inputUrl" varchar not null,
  "outputUrl" varchar not null
);

# --- !Downs

drop table "urls" if exists;
