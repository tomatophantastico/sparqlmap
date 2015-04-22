

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test case puts the dump class under test with various invalid mappings
 * to check for prio validation.
 * 
 * @author joerg
 * 
 */
public class dumpTest {
	
	
	@Test
	public void testGenerateMapping() {
		String[] params = { "-generateMapping","-r2rmlfile",
				"./src/test/resources/map-conf/mapping.ttl",
				"-dbfile", "./src/test/resources/map-conf/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains("Not a valid token"));
	}
	
	@Test
	public void testDump() {
		String[] params = { "-dump","-r2rmlfile",
				"./src/test/resources/map-conf/mapping.ttl",
				"-dbfile", "./src/test/resources/map-conf/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains("Not a valid token"));
	}

	@Test
	public void testmappinginvalidttl() {

		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_invalidtl.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains("Not a valid token"));
	}

	private String performCommand(String[] params) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		
		sparqlmap sm = new sparqlmap(new PrintStream(baos),new PrintStream(baos));
		try {
      sm.processCommand(params);
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

		
		String output = baos.toString();
		System.out.print(output);
		return output;
	}
	
	@Test
	public void testnosubject(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_nosubject.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"has no subject term map"));

	}
	@Test
	public void testnovalidsubject(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_nosubjecttemplate.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"has no subject term map"));

	}
	
	@Test
	public void testNoPos(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_no_pos.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"xxxx"));

	}
	@Test
	public void testNologicalTable(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_no_logical_table.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"xxxx"));

	}
	@Test
	public void testNonesitantLogicalTable(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_nonexistant_logical_table.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"xxxx"));

	}
	@Test
	public void testMissingObjectMap(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_missingobjectMap.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"xxxx"));

	}
	@Test
	public void testMissingPredicateMap(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_missingpredicate.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"xxxx"));

	}
	@Test
	public void testtypeinobject(){
		String[] params = { "-r2rmlfile",
				"./src/test/brokenbsbmmappings/mapping_typoinobjectmap.ttl",
				"-dbfile", "./src/test/conf/bsbm/db.properties" };
		String output = performCommand(params);

		Assert.assertTrue(output.toString().contains(
				"xxxx"));

	}
}

