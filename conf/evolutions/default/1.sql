# --- !Ups

create table "PROPERTIES" ("NAME" VARCHAR NOT NULL PRIMARY KEY, "VALUE" VARCHAR);

# --- !Downs

drop table "PROPERTIES";

