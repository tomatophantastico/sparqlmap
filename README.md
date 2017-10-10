# SparqlMap


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
Is quite simple, just provide the ```--r2rmlFile```parameter: 
```
--r2rmlFile=<fileLocation>
```

### Creation of a Mapping

Creating a R2RML representation of a default mapping is as easy as this, just change the action:

```shell
./bin/sparqlmap --action=directmapping --ds.type=JDBC --ds.url="jdbc:mysql://192.168.59.103:3306/sparqlmaptest" --ds.username=sparqlmap  --ds.password=sparqlmap  
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
SparqlMap allows the following command line interactions, selected by the `--action=<action>` parameter

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
Existing mapping files can be provided via the r2rmlFile-parameter:
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


# Custom R2RML extensions

## Conditional Mappings and transformations
SparqlMap allows you to perform to define regex based conditionals and transformations for cleaning data.
These options are only evaluated when materializing data and is therefore note available in the SQL backend.
Also, please bear in mind that complex matching and replacement patterns may seriously degrade performance, although if you need to do this kind of transformation, using the mapping tool is certainly the most efficient way to do so.

Lets consider a simple example, which can also be found in the test suite.
Given some hancrafted data about events in a CSV sheet, we want expose the data as highest possible quality and therefore put all the knowledge we got into the mapping.
When examining the data, we notice three different kind of date notations here:
´´´
id,date
1,10.10.2016
2,12/23/2016
3,2016 (exact date unknown)
´´´

* Entry 1 uses a notation with dots as separators, as we are unsure about the notation we want to use the property ´vocab:datewithdot' property.
* Entry 2 we recognize as a date and we want to transform it into an ´xsd:date´
* Entry 3 contains literals and requires manual editing. Therefore we want to use the separate property ´vocab:dateString´


### Conditional TermMaps (Entry 3)
Conditions are expressed as regexes and are matched against the String-representation of an RDF-Term, e.g., denending on the type of TermMap used:
* Template based Term Maps materialize the template, any 
* Column-based attempts to convert the content into a string (i.e. a timestamp becomes a xsd:date string, binary gets hex-encoded) and match the pattern
In any case, term type, language and literal lype are ignored.

So in order to map the textually desribed dates with the property ´vocab:dateString´ we use the ´smap:requiredPattern´ Property in the term map.
´´´
r2rml:predicateObjectMap
    [ r2rml:objectMap
              [ r2rml:column "\"date\"";
                smap:requiredPattern """^(.*)[a-zA-Z](.*)$"""] ;
      r2rml:predicate
              vocab:dateString;

    ] ;
´´´
The object term will only be generated, if the pattern matches the whole string. Technically, this is implementd using Javas String.matches(String requiredPattern).
Other R2RML processors will ignore ´smap:requiredPattern´.


### Maintain compatibility with other R2RML processors (Entry 1)
As other R2RML processors will ignore the ´smap:requiredPattern´ Property, multiple and perphaps invalid values would be generated, if a column is mapped with multiple ´r2rml:predicateObjectMaps´.
In order to be able to express a safe, r2rml compliant fallback in mappings, we subclassed/subpropertied R2RML concepts, such that us can express the generic/fallback mapping using the standard r2rml vocab and the specific cases using the sparqmap vocab.

In our concrete Example, we want to map Entry 1 with a different property.
We simply add an other ´predicateObjectMap´ with an other ´smap:requiredPattern´.

´´´
 r2rml:predicateObjectMap
    [ r2rml:objectMap
              [ r2rml:column
                        "\"date\"";
                smap:requiredPattern """^(.*)[a-zA-Z](.*)$"""] ;
      r2rml:predicate
              vocab:dateString;

    ] ;
 smap:predicateObjectMap
    [ smap:objectMap
              [ smap:column
                        "\"date\""; 
                smap:requiredPattern """^([0-9]{1,2})\\.([0-9]{1,2})\\.([0-9]{2,4})$"""
              ] ;
      smap:predicate
              vocab:datewithdot;

    ] ;
´´´

Mind here the use of a different prefix, namely ´smap:predicateObjectMap´ instead of ´r2rml:predicateObjectMap´.
This is purely for better compatibility with other R2RML rewriters, as the with the ´smap´ the more specific mapping can be expressed which will be ignored by other R2RML rewriters.



### TermMap transformation (Entry 2)

Entry 2 marks the case, where a small transformation is required to fit into a standard data type. 
This can be achieved with a pattern provided via the ´smap:transformPattern´. It allows the use of regex with capturing groups, defined in the requiredPattern,  for small transformations.

Technically, this is achieved with Javas String.replaceAll(String requiredPattern, String replacement).

See this example:
´´´
smap:predicateObjectMap
    [ smap:objectMap
              [ smap:column "\"date\""; 
                smap:requiredPattern """^([0-9]{1,2})\\/([0-9]{1,2})\\/([0-9]{2,4})$""";
                smap:transformPattern """$1-$2-$3""";
                r2rml:datatype xsd:date;
              ] ;
      smap:predicate
              vocab:date ;
    ] ;
´´´





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

##Excel-Files
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
##CouchDB
```
--ds.type=COUCHDB  Mandatory
--ds.url=<httpurl> required  The url of the couchdbserver (using ektorp)
```
Optionally:
```
--ds.username=<username>
--ds.password=<password>
```
