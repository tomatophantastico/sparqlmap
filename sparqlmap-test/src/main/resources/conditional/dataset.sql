create table "event"(
"id" integer,
"name" LONGVARCHAR,
"date" LONGVARCHAR
);

insert into "event" ("id", "name", "date") VALUES (1,'event1','10.10.16');
insert into "event" ("id", "name", "date") VALUES (2,'event2','12/10/2016');
insert into "event" ("id", "name", "date") VALUES (3,'event 3','2016 (exact date unknown)');
insert into "event" ("id", "name", "date") VALUES (4,'event 4',NULL);
insert into "event" ("id", "name", "date") VALUES (5,'XXX','13/10/2016');
