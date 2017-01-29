CREATE TABLE "thing1" ("id" VARCHAR(200), "label" VARCHAR(200));
CREATE TABLE "thing2" ("id" VARCHAR(200), "label" VARCHAR(200),"subof" VARCHAR(200));
CREATE TABLE "thing3" ("id" VARCHAR(200), "label" VARCHAR(200),"subof" VARCHAR(200));

INSERT INTO "thing1" ("id","label") VALUES ('thing1_1','thing1_1_desc');
INSERT INTO "thing1" ("id","label") VALUES ('thing1_2','thing1_2_desc');
INSERT INTO "thing1" ("id","label") VALUES ('thing1_3','thing1_3_desc');

INSERT INTO "thing2" ("id","label","subof") VALUES ('thing2_1','thing2_1_desc','thing1_1');
INSERT INTO "thing2" ("id","label","subof") VALUES ('thing2_2','thing2_2_desc','thing1_1');
INSERT INTO "thing2" ("id","label","subof") VALUES ('thing2_3','thing2_3_desc','thing1_3');

INSERT INTO "thing3" ("id","label","subof") VALUES ('thing3_1','thing3_1_desc','thing2_3');
INSERT INTO "thing3" ("id","label","subof") VALUES ('thing3_2','thing3_2_desc','thing2_3');
INSERT INTO "thing3" ("id","label","subof") VALUES ('thing3_3','thing3_3_desc','thing2_3');