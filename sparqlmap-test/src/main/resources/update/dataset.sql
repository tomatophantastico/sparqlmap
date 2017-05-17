create table "EVENT"(
"ID" integer,
"NAME" LONGVARCHAR,
);

create table "ARTIST"(
"ID" integer,
"NAME" LONGVARCHAR
)

create table "ARTIST_EVENT"(
"A_ID" integer,
"E_ID" integer
)


insert into "EVENT" ("ID", "NAME") VALUES (1,'event1');
insert into "EVENT" ("ID", "NAME") VALUES (2,'event2');
insert into "EVENT" ("ID", "NAME") VALUES (3,'event3');

insert into "ARTIST" ("ID", "NAME") VALUES (1,'artist1');
insert into "ARTIST" ("ID", "NAME") VALUES (2,'artist2');

insert into "ARTIST_EVENT" ("A_ID","E_ID") VALUES (1,1);
insert into "ARTIST_EVENT" ("A_ID","E_ID") VALUES (1,2);
insert into "ARTIST_EVENT" ("A_ID","E_ID") VALUES (1,3);
insert into "ARTIST_EVENT" ("A_ID","E_ID") VALUES (2,3);
