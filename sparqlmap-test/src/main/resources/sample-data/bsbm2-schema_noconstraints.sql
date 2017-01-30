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

--SET default_with_oids = false;

--
-- Name: offer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE offer (
    nr integer NOT NULL,
    product integer,
    producer integer,
    vendor integer,
    price double precision,
    "validFrom" timestamp without time zone,
    "validTo" timestamp without time zone,
    "deliveryDays" integer,
    "offerWebpage" character varying(100),
    publisher integer,
    "publishDate" date
);


-- ALTER TABLE public.offer OWNER TO postgres;

--
-- Name: person; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE person (
    nr integer NOT NULL,
    name character varying(30),
    mbox_sha1sum character(40),
    country character(2),
    publisher integer,
    "publishDate" date
);


-- ALTER TABLE public.person OWNER TO postgres;

--
-- Name: producer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE producer (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    homepage character varying(100),
    country character(2),
    publisher integer,
    "publishDate" date
);


-- ALTER TABLE public.producer OWNER TO postgres;

--
-- Name: product; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE product (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    producer integer,
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
    publisher integer,
    "publishDate" date
);


--ALTER TABLE public.product OWNER TO postgres;

--
-- Name: productfeature; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productfeature (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    publisher integer,
    "publishDate" date
);


-- ALTER TABLE public.productfeature OWNER TO postgres;

--
-- Name: productfeatureproduct; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE productfeatureproduct (
    product integer NOT NULL,
    productfeature integer NOT NULL
);


--ALTER TABLE public.productfeatureproduct OWNER TO postgres;

--
-- Name: producttype; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE producttype (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    parent integer,
    publisher integer,
    "publishDate" date
);


--ALTER TABLE public.producttype OWNER TO postgres;

--
-- Name: producttypeproduct; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE producttypeproduct (
    product integer NOT NULL,
    producttype integer NOT NULL
);


--ALTER TABLE public.producttypeproduct OWNER TO postgres;

--
-- Name: review; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE review (
    nr integer NOT NULL,
    product integer,
    producer integer,
    person integer,
    "reviewDate" timestamp without time zone,
    title character varying(200),
    text text,
    language character(2),
    rating1 integer,
    rating2 integer,
    rating3 integer,
    rating4 integer,
    publisher integer,
    "publishDate" date
);


--ALTER TABLE public.review OWNER TO postgres;

--
-- Name: vendor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE vendor (
    nr integer NOT NULL,
    label character varying(100),
    comment character varying(2000),
    homepage character varying(100),
    country character(2),
    publisher integer,
    "publishDate" date
);


--ALTER TABLE public.vendor OWNER TO postgres;

--
-- Name: offer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

