package org.aksw.sparqlmap.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.sparqlmap.cli.SparqlMapStarter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.jooq.lambda.Seq;

import com.google.common.base.Splitter;

/**
 * Created by joerg on 01.08.17.
 */
public class  CliTestWrapper {

    PrintStream sysout = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream dmout = new PrintStream(baos);

    PrintStream ssyserr = System.err;
    ByteArrayOutputStream boaerr = new ByteArrayOutputStream();
    PrintStream dmerr = new PrintStream(boaerr);



    protected  void test(List<String> params){
        System.setOut(dmout);
        System.setErr(dmerr);

        SparqlMapStarter.main(params.toArray(new String[0]));
        dmout.flush();
        dmerr.flush();
        System.setOut(sysout);
        System.setErr(ssyserr);
    }

    public Model outputAsModel(Lang lang){

        Model dm_model = ModelFactory.createDefaultModel();

        RDFDataMgr.read(dm_model, new ByteArrayInputStream(baos.toByteArray()), lang);
        return dm_model;

    }

    public List<String> outputAsString(){
        return Seq.seq(Splitter.on(System.lineSeparator()).trimResults().omitEmptyStrings().split(baos.toString())).collect(Collectors.toList());

    }

    public  String errAsString(){
        return boaerr.toString();
    }

    public List<String> errAsList(){
        return Seq.seq(Splitter.on(System.lineSeparator()).trimResults().omitEmptyStrings().split(boaerr.toString())).collect(Collectors.toList());

    }

}
