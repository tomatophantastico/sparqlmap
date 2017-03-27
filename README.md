#SparqlMap - Client


SparqlMap - A SPARQL to SQL rewriter based on [R2RML](http://www.w3.org/TR/r2rml/) specification.

It can be used in allows both extracting RDF from an relational database and rewrite SPARQL queries into SQL.

The SparqlMap Client provides command line and web access to the SparqlMap core.

## Download

Get the latest release on the [release page](https://github.com/tomatophantastico/sparqlmap/releases).
Please note, that no MySQL driver is included. You will need to get it from the [MySQL page](https://dev.mysql.com/downloads/connector/j/)  and copy it into the ./lib folder.


## Building

We use a [patched version](https://github.com/tomatophantastico/metamodel) of [Apache Metamode](http://metamodel.apache.org/), which improves quotation mark handling.

To include it into your SparqlMap-build please install it locally (using [maven](http://maven.apache.org))
```
git clone --branch feature/quoteColumns https://github.com/tomatophantastico/metamodel && cd metamodel && mvn install
```

SparqlMap utilizes gradle, so you can just run
```
./gradle installDist
```
which will create a distribution in `build/install/sparqlmap`.





## Overview over the mapping process

![SparqlMap overview](https://raw.github.com/tomatophantastico/sparqlmap/doc/doc/sparqlMap.png)



## Quick Start

Most of the time, dump creation will take place on the command line.
In the binary distributions use the sparqlmap command. 
Calling sparqlmap without or with a wrong combination of options will present all options available.

Let's have a look at some samples:

### RDF Dump

```shell
./bin/sparqlmap --action=dump --ds.type=JDBC --ds.url="jdbc:mysql://192.168.59.103:3306/sparqlmaptest" --ds.username=sparqlmap --ds.password=sparqlmap  --r2rmlFile=src/test/resources/hsql-bsbm/mapping.ttl   
```
Or if you do not have a R2RML mapping, you can create a dump based on a Direct Mapping

```shell
./bin/sparqlmap --action=dump --ds.type=JDBC --ds.url="jdbc:mysql://192.168.59.103:3306/sparqlmaptest" --ds.username=sparqlmap --ds.password=sparqlmap 
```


## R2RML Mappings

### Re-use an Existing Mapping
Is quite simple, just provide the ```-r2rml.file```parameter: 
```
-r2rml.file=<fileLocation>
```

### Creation of a Mapping

Creating a R2RML representation of a default mapping is as easy as this, just change the action:

```shell
./bin/sparqlmap --action=directmapping --ds.type=JDBC --ds.url="jdbc:mysql://192.168.59.103:3306/sparqlmaptest" -ds.username=sparqlmap  --ds.password=sparqlmap  
```

## Direct Mapping options
With the following options, the generation of the direct mapping can be controlled.
These options are meant to ease manual editing of the generated R2RML file, or when the 

* a given baseuriprefix is suffixed by either mapping, vocab or instance to produce the corresponding uri prefixes.
* the mappinguriprefix will only show up in the resulting r2rml-file for name the R2RML Resources, such as TriplesMaps
* vocaburiprefix helps constructing useful identifiers for predicates and class generated from the schema of the data source. It will therefore show up in the extraced data.
* Resources generated in the RDB-to-RDF translation process use the instanceuriprefix for IRI generation.

```
--dm.baseuriprefix=<baseuri/>
--dm.mappinguriprefix=<baseuri/mapping/>
--dm.vocaburiprefix=<basuri/vocab/>
--dm.instanceuriprefix=<baseuri/instance>
--dm.separatorchar=#
```






## Rewrite SPARQL queries into SQL

For rewriting SPARQL queries into SQL SparqlMap can expose a SPARQL endpoint by an embedded tomcat.
The enpoint is started by 

This will expose an SPARQL endpoint with a little snorql interface.



## R2RML conformance

SparqlMap conforms with the R2RML specification and was tested with PostgreSQL, MySQL and HSQL.


## Adding additional database drivers

Simply copy them into the lib folder.

## Building and installation

SparqlMap requires the following dependencies, which are not present in the main repos.

2. Metamodel with a patch
```shell
git clone https://github.com/tomatophantastico/metamodel.git
cd metamodel
mvn install
```


# Actions

## CLI
SparqlMap allows the following command line interactions, selected by the `--action=<action>´ parameter

### dump

This creates a dump of the mapped database. You can specify the outputformat, using the `--format=<FORMAT>` parameter.
Supported is TURTLE,  TRIG, NTRIPLES and NQUADS.
Triple serializations simply ignore the graph definitions of the mappings. A triple that occurs in multiple graphs will consequently appear multiple times in the result.

### directmapping

A R2RML mapping is created, based on the concept of a direct mapping, always yields TURTLE output.

### query

A query is executed and the result is returned on the command line.

## web
SparqlMap can be used as a SPARQL enndpoint, for example for exposing a ACCESS database:

```shell
bin/sparqlmap --action=web --ds.type=ACCESS --ds.url=mydata.accdb
``` 

The endpoint will be accessible on:
```
localhost:8080/sparql
```
Currently, a number of limitations apply:
* Some the query processing is executed in-memory, which degrades performance
* There is no further UI for entering and viewing queries



# Mapping Options
Existing mapping files can be provided via the r2rmlfile-parameter:
```
--r2rmlFile=<filename>
```
If this parameter is omitted, a direct mapping will be created.

The direct mapping generation can be modified by following attributes.
If you just define the  ```<baseUriPrefix>``` you should be fine in most cases.
```
--dm.baseUriPrefix=http://localhost/baseiri/
--dm.mappingUriPrefix=<baseUriPrefix>/mapping/
--dm.vocabUriPrefix=<baseUriPrefix>/instance/
--dm.instanceUriPrefix=<baseUriPrefix>/vocab/
--dm.separatorChar=+
```
# Data Sources


## Using with MongoDB (```--ds.type=MONGODB2``` or ```--ds.type=MONGODB3```)
```
--ds.type=MONGODB2 or --ds.type=MONGODB3
--ds.url  the host and port of the mongodb server, e.g.: localhost:11111
--ds.dbname the database name
--ds.username  username
--ds.password  password
```

## Using with a JDBC database
First, make sure that a JDBC4 driver is in the classpath. SparqlMap already contains drivers for the most important FOSS RDBMs, for closed source RDBMs, they have to be added manually.

```
--ds.url  the full jdbc url, e.g. jdbc:mysql://localhost:3306/test
--ds.username  username of the RDBMS
--ds.password  password of the RDBMS
--ds.maxPoolSize max number of connections to the RDBMs, defaults to 10
```

## Using with CSV files (--ds.type=CSV)
For more details check the [Apache MetaModel CSV adapter wiki page](https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=65875503)

Required parameters
```
--ds.type=CSV If it is not CSV, you will have to look at your other options
--ds.url=<path> required  The path to the file
```
Optional parameters and their defaults
```
--ds.quoteChar=" Encloses values
--ds.separatorChar=, default to , values in a row are split according to this value
--ds.escapeChar=\ for escaping special characters
--ds.encoding=UTF-8 
--ds.columnNameLineNumber=1 Starting from 1, 
--ds.failOnInconsistentRowLength=true defaults to true if the column count varies in the file, this pushes the parser further.
--ds.multilineValues=false allows multiline values
```

## Excel-Files
```
--ds.type=EXCEL  Mandatory
--ds.url=<path> required  The path to the file
```
Optional
```
--ds.columnNameLineNumber=1
--ds.SkipEmptyLines=true
--ds.SkipEmptyColumns=true
```

## Access-Files
Besides the type, only the file location needs to be provided
```
--ds.type=ACCESS  Mandatory
--ds.url=<path>
```
## CouchDB
```
--ds.type=COUCHDB  Mandatory
--ds.url=<httpurl> required  The url of the couchdbserver (using ektorp)
```
Optionally:
```
--ds.username=<username>
--ds.password=<password>
```
