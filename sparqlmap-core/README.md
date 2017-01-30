# SparqlMap - core library
[![Build Status](https://travis-ci.org/tomatophantastico/sparqlmap-core.svg?branch=develop)](https://travis-ci.org/tomatophantastico/sparqlmap-core) 

SparqlMap - A SPARQL to Structured Data rewriter based on [R2RML](http://www.w3.org/TR/r2rml/) specification.

It can be used in allows both extracting RDF from an relational database and rewrite SPARQL queries into SQL.

## Usage

This module contains the core functionality of SparqlMap.

It serves as the foundation for the [SparqlMap client](http://github.com/tomatophantastico/sparqlmap). 

If you want to use it in your own application, you can simply start with:

SparqlMapFactory.create("http://mybaseuri").connectToCsv("/tmp/test.csv").mapWith



## DB Support
The data access is abstracted using [Apache MetaModel](http://metamodel.apache.org/) and therefore provides access to a huge selection of structured data sources, including:
or:
Relational Databases:
PostgreSQL, MySQL, Oracle, SQL Server, Apache Hive and Embedded DBs
Document and wide column Stores:
CouchDB, MongoDB
HBase,Cassandra
and others:
ElasticSearch, Salesforce.com, SugarCRM
CSV files, Spreadsheets, XML files, JSON files
 

## Restrictions
* R2RML binary support: There is no support for binary data types (xsd:binary) in HSQL due to problems with the query engine.
