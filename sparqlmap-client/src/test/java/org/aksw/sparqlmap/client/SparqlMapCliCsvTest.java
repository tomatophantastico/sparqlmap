package org.aksw.sparqlmap.client;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SparqlMapCliCsvTest {


    private static final String CSV_LOC = "../sparqlmap-test/src/main/resources/datacube/dataset.csv";



    @Test
    public void testFailingFormatParam(){
        String[] args = {"--action=dump", "--ds.type=CSV", "--ds.url=" + CSV_LOC, "--ds.separatorChar=;", "--ds.columnNameLineNumber=3","--format=NTRIPLExS"};

        CliTestWrapper wrapper = new CliTestWrapper();
        wrapper.test(Lists.newArrayList(args));
        Assert.assertTrue(wrapper.errAsString(),wrapper.errAsString().contains("Error"));

    }
}
