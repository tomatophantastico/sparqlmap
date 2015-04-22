import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.aksw.sparqlmap.core.ImplementationException;
import org.aksw.sparqlmap.core.SparqlMap;
import org.aksw.sparqlmap.core.automapper.AutomapperWrapper;
import org.aksw.sparqlmap.core.config.syntax.r2rml.R2RMLValidationException;
import org.aksw.sparqlmap.core.db.CSVHelper;
import org.aksw.sparqlmap.core.db.CSVHelper.CSVColumnConfig;
import org.aksw.sparqlmap.core.db.CSVHelper.CSVTableConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;

public class sparqlmap {

	static org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(sparqlmap.class);

	public sparqlmap() {
		this.out = System.out;
		this.err = System.err;
	}
	
	public sparqlmap(PrintStream out, PrintStream err) {
		super();
		this.out = out;
		this.err = err;
	}
	

	PrintStream out;
	PrintStream err;



	
	private Options options;
	private AnnotationConfigApplicationContext ctxt;

	public AnnotationConfigApplicationContext setupSparqlMap(
			Properties... props) throws Throwable {

		AnnotationConfigApplicationContext ctxt;

		ctxt = new AnnotationConfigApplicationContext();

		for (int i = 0; i < props.length; i++) {
			ctxt.getEnvironment()
					.getPropertySources()
					.addFirst(new PropertiesPropertySource("props " + i, props[i]));
		}

		ctxt.scan("org.aksw.sparqlmap");
		ctxt.refresh();
		return ctxt;

	}

	private Throwable getExceptionCause(BeansException be) {
		if (be.getCause() instanceof BeansException) {
			return getExceptionCause((BeansException) be.getCause());
		} else {
			return be.getCause();
		}
	}

	public static Options getOptions() {
		
		Options options = new Options();
		
		OptionGroup action = new OptionGroup();
		action.addOption(OptionBuilder.withDescription("Writes an rdf dump into stdout according to the supplied mapping file").create("dump"));
		action.addOption(OptionBuilder.withDescription("Creates a mapping file that maps the specified database into R2RML according to the direct mapping specification").create("generateMapping"));

		
		options.addOptionGroup(action);
		
		options.addOption(OptionBuilder
				.withArgName("db-file")
				.hasArg()
				.withDescription(
						"A file properties file containing the parameters for connecting to the database.")
				.create("dbfile"));
		options.addOption(OptionBuilder
				.withArgName("jdbc-url")
				.hasArg()
				.withDescription(
						"the full connection url for the database, like it is used for connection in java to a db. Example: jdbc:postgresql://localhost/mydb")
				.create("dburi"));
		options.addOption(OptionBuilder.withArgName("db-username").hasArg()
				.withDescription("username for the db connection")
				.create("dbuser"));
		options.addOption(OptionBuilder.withArgName("db-password").hasArg()
				.withDescription("password for the db connection")
				.create("dbpass"));
		options.addOption(OptionBuilder
				.withArgName("base-iri")
				.hasArg()
				.withDescription(
						"Base iri used for cases in the mapping process, where no explicit iri is defined.")
				.create("baseiri"));
		options.addOption(OptionBuilder.withArgName("r2rmlfile").hasArg().withDescription("The R2RML file according which defines the mapping for the dump.").create("r2rmlfile"));		
		options.addOption(OptionBuilder.withArgName("sparqlmapfile").hasArg().withDescription("A properties file that configures SparqlMap. Usually contains the properties given here as options. Explicit options override the values of the properties file.").create("sparqlmapfile"));
		
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("CSV File name").create("csvfile"));
		options.addOption(OptionBuilder.withDescription("The first line of the file describes the headers. For details check: http://hsqldb.org/doc/guide/texttables-chapt.html").create("csvhasheader"));
		options.addOption(OptionBuilder.withDescription("The file as rows with a varying count of columns").create("varyingcolcount"));
		options.addOption(OptionBuilder.withArgName("char").hasArg().withDescription("The char separator character. Defaults to ','.  For greater detail check: http://hsqldb.org/doc/guide/texttables-chapt.html").create("csvsepchar"));
		options.addOption(OptionBuilder.withArgName("char").hasArg().withDescription("The varcharchar separator character.Defaults to ','. For greater detail check: http://hsqldb.org/doc/guide/texttables-chapt.html").create("csvsepvarchar"));
		options.addOption(OptionBuilder.withArgName("encoding").hasArg().withDescription("The encoding of the csv file.").create("csvencoding"));
		
		options.addOption(OptionBuilder.withArgName("outputformat").hasArg().withDescription("The output format name. Values are: RDF/XML, Turtle, N-TRiples, N3, RDF/JSON, N-Quads, TriG. Defaults to N-Triples.").create("outputformat"));
		return options;
	}

	public void processCommand(String[] args) throws Throwable {
		try {
			CommandLineParser clparser = new PosixParser();
			// get the default
			options = getOptions();
			// get the command specific
			// process the commandline args
			CommandLine cl = null;

			cl = clparser.parse(options, args);

			// process the basic options
			Properties props = new Properties();
			
			//load sparqlmap properties from the classpath
			
			props.load(new ClassPathResource("sparqlmap.properties").getInputStream());
			
			if(cl.hasOption("generateMapping")){
			  props.remove("sm.mappingfile");
			}
			
			
			if (cl.hasOption("baseiri")) {
				props.setProperty("sm.baseuri", cl.getOptionValue("baseiri"));
			} else {
				props.setProperty("sm.baseuri", "http://localhost/baseuri/");
			}

			// process the database options
			processDbOptions(options, cl, props);
			String outputlang = null;
			if(cl.hasOption("outputformat")){
				outputlang =  cl.getOptionValue("outputformat");
			}
			
			
			// set the db mapping
			if(cl.hasOption("r2rmlfile")){
				props.setProperty("sm.mappingfile", cl.getOptionValue("r2rmlfile"));
			}
			

			//setup the context
			this.ctxt = setupSparqlMap(props);
			

			
			// perform the specified action
			
			if(cl.hasOption("dump")){
				System.err.println("Creating an RDF dump.");
				dump(outputlang);

			}else if(cl.hasOption("generateMapping")){
				
				
				//check if there is no mapping file given
				if(!cl.hasOption("r2rmlfile")){
					System.err.println("Creating R2RML based on Direct Mapping");
					generateMapping(outputlang);
				}else{
					error("for generateMapping do not use the -r2rmlfile option.");
				}
				
				
				
			} 

			
		} catch (ParseException e) {
			error(e.getMessage());
		} catch (FileNotFoundException e) {
			error("File not found: " + e.getMessage());
		} catch (IOException e) {
			error("Error reading file: " + e.getMessage());
		} catch (R2RMLValidationException e) {
			error("Error validation the mapping file: " + e.getMessage());
		} 
		
//		catch (BeansException e) {
//			err.println("Error during setup: "
//					+ getExceptionCause(e).getMessage());
//
//		} catch (Throwable t) {
//			log.error("Another error happened: ", t);
//			err.println("Error setting up the app: " + t.getMessage());
//
//		}

	}

	private void processDbOptions(Options options, CommandLine cl,
			Properties props) throws MissingOptionException,
			FileNotFoundException, IOException {
		if (cl.hasOption("dbfile")) {
			// load db file

			props.load(new FileReader(cl.getOptionValue("dbfile")));

		} else if (cl.hasOption("dbuser") && cl.hasOption("dbpass")
				&& cl.hasOption("dburi")) {
			//

			props.setProperty("jdbc.url", cl.getOptionValue("dburi"));
			props.setProperty("jdbc.username", cl.getOptionValue("dbuser"));
			props.setProperty("jdbc.password", cl.getOptionValue("dbpass"));
		} else if(cl.hasOption("csvfile")){
			String dbfilename = new File(cl.getOptionValue("csvfile")).getParent() + "/.hsql/" +new File(cl.getOptionValue("csvfile")).getName() + "";
			String jdbcstring = "jdbc:hsqldb:file:"+dbfilename;
			if(!new File(dbfilename + ".log").exists()){
				createCsvDatabase(cl,jdbcstring);
			}		
			props.setProperty("jdbc.url", jdbcstring);
			props.setProperty("jdbc.username", "SA");
			props.setProperty("jdbc.password", "");
		
			
		} else {
			throw new MissingOptionException(
					"Define either a database connection or a CSV file. Supply the db connection information by either supplying a file or the db options");
		}

		props.setProperty("jdbc.poolminconnections", "1");
		props.setProperty("jdbc.poolmaxconnections", "10");
	}

	private void createCsvDatabase(CommandLine cl, String jdbcstring) {

		CSVTableConfig table = new CSVTableConfig();
		table.name = "csv_import";
		table.file = new File(cl.getOptionValue("csvfile"));
		table.fs = cl.getOptionValue("csvsepchar", ",");
		
		table.consistentcolcount = !cl.hasOption("varyingcolcount");
		
		if(table.fs.length()>1){
			if(table.fs.equals("\\t")){
				table.fs_interpreted = '\t';
				
			}else{
				throw new ImplementationException("Implement separating character: " + table.fs);
			}
		}else{
			table.fs_interpreted = table.fs.charAt(0);
		}
		
		table.hasHeaderRow = cl.hasOption("csvhasheader");
		table.encoding = cl.getOptionValue("csvencoding", "UTF-8");
		
		
		
		String colstring =null;
		
		try{
			List<CSVColumnConfig> cccs = CSVHelper.getColumns(table);
			StringBuffer sb = new StringBuffer();
			
			for(CSVColumnConfig ccc: cccs){
				sb.append(String.format("\"%s\" %s ,", ccc.name , ccc.datatype));
			}
			colstring = sb.toString().substring(0, sb.toString().length()-1);
			
			
			
		
			
			
			
		}catch(IOException e){
			
			log.error("Error reading the csv file",e);
			throw new RuntimeException( e);
		}
		
		
			
		try {
			
			Connection c = DriverManager.getConnection(jdbcstring, "SA", "");

			c.createStatement().execute(
					String.format("CREATE TEXT TABLE \"%s\"(%s)",table.name,colstring));

			String options = String
					.format("%s;encoding=%s;fs=%s;ignore_first=%s;all_quoted=%s;cache_rows=%s;cache_size=%s",
							table.file.getAbsolutePath(), table.encoding, table.fs, table.hasHeaderRow, table.allQuoted,
							table.cacheRows, table.cacheSize);

			c.createStatement()
					.execute(
							"SET TABLE \"" + table.name + "\" SOURCE '"
									+ options + "'");

						
			
		} catch (SQLException e) {
			log.error("Error setting up the csv database wrapper", e);
		}	
//		finally{
//			try {
//				Connection c = DriverManager.getConnection(jdbcstring
//						+ ";shutdown=true", "SA", "");
//			} catch (SQLException e) {
//				log.error("Error closing the database");
//			}
//		}
		
	}

	public void error(String message) {
		HelpFormatter formatter = new HelpFormatter();
		err.println(message);
		PrintWriter pw = new PrintWriter(out);
		formatter.printHelp("java -jar <pathtojar> ", options);
		pw.flush();
	}


	
	
	
	
	public void dump(String langoutputf) throws SQLException{
		if(langoutputf==null){
			langoutputf = WebContent.contentTypeNTriples;
		}
		
		SparqlMap sm = ctxt.getBean(SparqlMap.class);
		
		sm.dump(System.out, langoutputf);
		
	}
	

	
	
	public void generateMapping(String format) throws FileNotFoundException, SQLException{
		
		AutomapperWrapper am = ctxt.getBean(AutomapperWrapper.class);
		
		if(format==null){
			format = WebContent.contentTypeTurtle;
		}
		
		if (!format.equals(Lang.TURTLE)){
			log.warn("For generating a mapping TURTLE is the recommended output format. ");
		}
		
				
		RDFDataMgr.write(out, am.automap(), RDFLanguages.nameToLang(format));
		
	}

	
	
	public static void main(String[] args) throws Throwable {
		
		sparqlmap smcl = new sparqlmap();
		smcl.processCommand(args);
		
	}
}
