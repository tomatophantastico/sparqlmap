
--
-- Name: offer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY offer
    ADD CONSTRAINT offer_pkey PRIMARY KEY (nr);


--
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (nr);


--
-- Name: producer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY producer
    ADD CONSTRAINT producer_pkey PRIMARY KEY (nr);


--
-- Name: product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY product
    ADD CONSTRAINT product_pkey PRIMARY KEY (nr);


--
-- Name: productfeature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productfeature
    ADD CONSTRAINT productfeature_pkey PRIMARY KEY (nr);


--
-- Name: productfeatureproduct_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY productfeatureproduct
    ADD CONSTRAINT productfeatureproduct_pkey PRIMARY KEY (product, productfeature);


--
-- Name: producttype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY producttype
    ADD CONSTRAINT producttype_pkey PRIMARY KEY (nr);


--
-- Name: producttypeproduct_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY producttypeproduct
    ADD CONSTRAINT producttypeproduct_pkey PRIMARY KEY (product, producttype);


--
-- Name: review_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY review
    ADD CONSTRAINT review_pkey PRIMARY KEY (nr);


--
-- Name: vendor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY vendor
    ADD CONSTRAINT vendor_pkey PRIMARY KEY (nr);


--
-- Name: offer_producer_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX offer_producer_product ON offer USING btree (producer, product, nr);


--
-- Name: offer_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_product ON offer USING btree (product, "deliveryDays");


--
-- Name: offer_validto; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_validto ON offer USING btree ("validTo");


--
-- Name: offer_vendor_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_vendor_product ON offer USING btree (vendor, product);


--
-- Name: offer_webpage; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX offer_webpage ON offer USING btree ("offerWebpage");


--
-- Name: pfeature_inv; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX pfeature_inv ON productfeatureproduct USING btree (productfeature, product);


--
-- Name: producer_country; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX producer_country ON producer USING btree (country);


--
-- Name: producer_homepage; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX producer_homepage ON producer USING btree (homepage);


--
-- Name: product_label_nr; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_label_nr ON product USING btree (label text_pattern_ops, nr);


--
-- Name: product_lbl; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_lbl ON product USING btree (label);


--
-- Name: product_pn1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_pn1 ON product USING btree ("propertyNum1");


--
-- Name: product_pn2; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_pn2 ON product USING btree ("propertyNum2");


--
-- Name: product_pn3; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX product_pn3 ON product USING btree ("propertyNum3");


--
-- Name: product_producer_nr; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX product_producer_nr ON product USING btree (producer, nr);


--
-- Name: ptype_inv; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX ptype_inv ON producttypeproduct USING btree (producttype, product);


--
-- Name: review_person; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX review_person ON review USING btree (person);


--
-- Name: review_person_1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX review_person_1 ON review USING btree (person, product, title);


--
-- Name: review_producer_product; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX review_producer_product ON review USING btree (producer, product, nr);


--
-- Name: review_product_person_producer; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX review_product_person_producer ON review USING btree (product, person, producer, nr);


--
-- Name: review_textlang; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX review_textlang ON review USING btree (language);


--
-- Name: vendor_country; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX vendor_country ON vendor USING btree (country);


--
-- Name: vendor_homepage; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX vendor_homepage ON vendor USING btree (homepage);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

