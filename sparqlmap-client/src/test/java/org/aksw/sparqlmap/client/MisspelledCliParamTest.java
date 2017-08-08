package org.aksw.sparqlmap.client;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class MisspelledCliParamTest {

    @Test
    public void testWrongPrefix(){
        CliTestWrapper wrap = new CliTestWrapper();

        wrap.test(Lists.newArrayList("--action=dump","--ds.url=../../notThere.csv","--ds.type=CSV", "--dm.vocoburiprefix=http://example.com/vocabPrefix"));

        List<String> errors = wrap.errAsString();


        Assert.assertFalse(errors.stream().anyMatch(errLine -> errLine.contains("Exception")));


    }
}
