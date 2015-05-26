#SparqlMap - Client


SparqlMap - A SPARQL to SQL rewriter based on [R2RML](http://www.w3.org/TR/r2rml/) specification.

It can be used in allows both extracting RDF from an relational database and rewrite SPARQL queries into SQL.

The SparqlMap Client provides command line and web access to the SparqlMap core.


## Overview over the mapping process

![SparqlMap overview](https://raw.github.com/tomatophantastico/sparqlmap/doc/doc/sparqlMap.png)



##Convert Relational Database into RDF

Most of the time, dump creation will take place on the command line.
In the binary distributions these can be found in the _bin_ folder.
The two files of interest are:

```shell
./bin/dump.sh             (1)
./bin/generateMapping.sh  (2)
```

(1) dump will create an full RDF representation based on a mapping provided.
(2) will generate that mapping for you, based on the [Direct Mapping](http://www.w3.org/TR/rdb-direct-mapping/) specification. You can use this mapping as a starting point. However, be aware that the full database will be mapped and that the mapping will apear clumsy.

## Rewrite SPARQL queries into SQL

For rewriting SPARQL queries into SQL SparqlMap can expose a SPARQL endpoint by an embedded tomcat.
The enpoint is started by 
```shell
java -jar sparqlmap-X.X.X-with-dependencies.jar
```
This will expose an SPARQL endpoint with a little snorql interface.

## R2RML conformance

SparqlMap conforms fully with the R2RML specification and was tested with PostgreSQL, MySQL and HSQL.
The test results can be found in the repository.
