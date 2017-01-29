--
-- PostgreSQL database dump
--

--SET statement_timeout = 0;
--SET client_encoding = 'UTF8';
--SET standard_conforming_strings = off;
--SET check_function_bodies = false;
--SET client_min_messages = warning;
--SET escape_string_warning = off;

--SET search_path = public, pg_catalog;

--SET default_tablespace = '';

---SET default_with_oids = false;

--
-- Name: offer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "offer" (
    "nr" integer NOT NULL,
    "product" integer,
    "producer" integer,
    "vendor" integer,
    "price" double precision,
    "validFrom" date,
    "validTo" date,
    "deliveryDays" integer,
    "offerWebpage" character varying(100),
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.offer OWNER TO postgres;

--
-- Name: person; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "person" (
    "nr" integer NOT NULL,
    "name" character varying(30),
    "mbox_sha1sum" character(40),
    "country" character(2),
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.person OWNER TO postgres;

--
-- Name: producer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "producer" (
    "nr" integer NOT NULL,
    "label" character varying(100),
    "comment" character varying(2000),
    "homepage" character varying(100),
    "country" character(2),
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.producer OWNER TO postgres;

--
-- Name: product; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "product" (
    "nr" integer NOT NULL,
    "label" character varying(100),
    "comment" character varying(2000),
    "producer" integer,
    "propertyNum1" integer,
    "propertyNum2" integer,
    "propertyNum3" integer,
    "propertyNum4" integer,
    "propertyNum5" integer,
    "propertyNum6" integer,
    "propertyTex1" character varying(250),
    "propertyTex2" character varying(250),
    "propertyTex3" character varying(250),
    "propertyTex4" character varying(250),
    "propertyTex5" character varying(250),
    "propertyTex6" character varying(250),
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.product OWNER TO postgres;

--
-- Name: productfeature; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "productfeature" (
    "nr" integer NOT NULL,
    "label" character varying(100),
    "comment" character varying(2000),
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.productfeature OWNER TO postgres;

--
-- Name: productfeatureproduct; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "productfeatureproduct" (
    "product" integer NOT NULL,
    "productfeature" integer NOT NULL
);


--ALTER TABLE public.productfeatureproduct OWNER TO postgres;

--
-- Name: producttype; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "producttype" (
    "nr" integer NOT NULL,
    "label" character varying(100),
    "comment" character varying(2000),
    "parent" integer,
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.producttype OWNER TO postgres;

--
-- Name: producttypeproduct; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "producttypeproduct" (
    "product" integer NOT NULL,
    "producttype" integer NOT NULL
);


--ALTER TABLE public.producttypeproduct OWNER TO postgres;

--
-- Name: review; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "review" (
    "nr" integer NOT NULL,
    "product" integer,
    "producer" integer,
    "person" integer,
    "reviewDate" date,
    "title" character varying(200),
    "text" longvarchar,
    "language" character(2),
    "rating1" integer,
    "rating2" integer,
    "rating3" integer,
    "rating4" integer,
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.review OWNER TO postgres;

--
-- Name: vendor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "vendor" (
    "nr" integer NOT NULL,
    "label" character varying(100),
    "comment" character varying(2000),
    "homepage" character varying(100),
    "country" character(2),
    "publisher" integer,
    "publishDate" date
);


--ALTER TABLE public.vendor OWNER TO postgres;

--
-- Name: offer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE "offer"
    ADD CONSTRAINT offer_pkey PRIMARY KEY ("nr");


--
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "person"
    ADD CONSTRAINT person_pkey PRIMARY KEY ("nr");


--
-- Name: producer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "producer"
    ADD CONSTRAINT producer_pkey PRIMARY KEY ("nr");


--
-- Name: product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "product"
    ADD CONSTRAINT product_pkey PRIMARY KEY ("nr");


--
-- Name: productfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "productfeature"
    ADD CONSTRAINT productfeature_pkey PRIMARY KEY ("nr");


--
-- Name: productfeatureproduct_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "productfeatureproduct"
    ADD CONSTRAINT productfeatureproduct_pkey PRIMARY KEY ("product", "productfeature");


--
-- Name: producttype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "producttype"
    ADD CONSTRAINT producttype_pkey PRIMARY KEY ("nr");


--
-- Name: producttypeproduct_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "producttypeproduct"
    ADD CONSTRAINT producttypeproduct_pkey PRIMARY KEY ("product", "producttype");


--
-- Name: review_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "review"
    ADD CONSTRAINT review_pkey PRIMARY KEY ("nr");


--
-- Name: vendor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE  "vendor"
    ADD CONSTRAINT vendor_pkey PRIMARY KEY ("nr");


--
-- Name: offer_producer_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX offer_producer_product ON "offer" ("producer", "product", "nr");


--
-- Name: offer_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_product ON "offer"  ("product", "deliveryDays");


--
-- Name: offer_validto; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_validto ON "offer" ("validTo");


--
-- Name: offer_vendor_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_vendor_product ON "offer" ("vendor", "product");


--
-- Name: offer_webpage; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_webpage ON "offer"  ("offerWebpage");


--
-- Name: pfeature_inv; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX pfeature_inv ON "productfeatureproduct"  ("productfeature", "product");


--
-- Name: producer_country; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX producer_country ON "producer"  ("country");


--
-- Name: producer_homepage; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX producer_homepage ON "producer"  ("homepage");


--
-- Name: product_label_nr; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_label_nr ON "product"  ("label", "nr");


--
-- Name: product_lbl; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_lbl ON "product"  ("label");


--
-- Name: product_pn1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_pn1 ON "product"  ("propertyNum1");


--
-- Name: product_pn2; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_pn2 ON "product" ("propertyNum2");


--
-- Name: product_pn3; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_pn3 ON "product"  ("propertyNum3");


--
-- Name: product_producer_nr; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX product_producer_nr ON "product"  ("producer", "nr");


--
-- Name: ptype_inv; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX ptype_inv ON "producttypeproduct" ("producttype", "product");


--
-- Name: review_person; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX review_person ON "review" ("person");


--
-- Name: review_person_1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX review_person_1 ON "review" ("person", "product", "title");


--
-- Name: review_producer_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX review_producer_product ON "review"  ("producer", "product", "nr");


--
-- Name: review_product_person_producer; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX review_product_person_producer ON "review"  ("product", "person", "producer", "nr");


--
-- Name: review_textlang; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX review_textlang ON "review"  ("language");


--
-- Name: vendor_country; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX vendor_country ON "vendor"  ("country");


--
-- Name: vendor_homepage; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX vendor_homepage ON "vendor" ("homepage");


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

--REVOKE ALL ON SCHEMA public FROM PUBLIC;
--REVOKE ALL ON SCHEMA public FROM postgres;
--GRANT ALL ON SCHEMA public TO postgres;
--GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

