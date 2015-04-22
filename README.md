#SparqlMap


SparqlMap - A SPARQL to SQL rewriter based on [R2RML](http://www.w3.org/TR/r2rml/) specification.

It can be used in allows both extracting RDF from an relational database and rewrite SPARQL queries into SQL.

##Current Status

We have refactored SparqlMap in the last few months a lot.
The current master branch is not yet ready for prime time, thus no binary release are out yet.

If you want to try out SparqlMap, best use a binary distribution from our [old website] (https://code.google.com/p/sparqlmap/downloads/list).



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

## Project Structure

This project has the following sub-folders:

- **sparqlmap-core** The rewriting (and the dumping) mechanics can be found here.
- **sparqlmap-web** The code for exposing webservices resides here.
- **sparqlmap-webinterface** The yeoman client-side webinterface is here.
- **sparqlmap-shared** Common resources, like exemplary mappings, local versions of vocabularies and default config files are here.
- **sparqlmap-cli** The command line wrapper around thecore.
- **sparqlmap-r2rmltestcases** The SparqlMap R2RML testrunner and the results of these test are here.
