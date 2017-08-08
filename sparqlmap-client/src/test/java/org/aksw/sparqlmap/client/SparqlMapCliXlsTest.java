package org.aksw.sparqlmap.client;

import org.aksw.sparqlmap.ResultHelper;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import java.util.List;

/**
 * Created by joerg on 30.07.17.
 */
public class SparqlMapCliXlsTest {


    private static String source = "../sparqlmap-test/src/main/resources/pfarrerbuch/KPS.xlsx";
    private static String mapping = "../sparqlmap-test/src/main/resources/pfarrerbuch/mapping_xls.ttl";
    private static String refrenceDump = "../sparqlmap-test/src/main/resources/pfarrerbuch/reference_xls.ttl";

    @Test
    public void testDirectMappingXls(){

        CliTestWrapper wrapper = new CliTestWrapper();

        List<String> params = Lists.newArrayList("--action=dump","--ds.url="+ source, "--r2rmlfile="+mapping , "--ds.columnNameLineNumber=1", "--ds.type=EXCEL" );

        wrapper.test(params);


        ResultHelper.assertModelAreEqual(wrapper.outputAsModel(Lang.TTL), RDFDataMgr.loadModel(refrenceDump));
    }
}
