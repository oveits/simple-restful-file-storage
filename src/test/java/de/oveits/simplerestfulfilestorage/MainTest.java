package de.oveits.simplerestfulfilestorage;

import java.net.URLDecoder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
//import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class MainTest {

	/**
	 * @param args
	 */
	
	public static void main(final String[] args) throws Exception {
		// if only option is to start ALL tests:
//	    JUnitCore.main(
//	    	"de.oveits.simplerestfulfilestorage.ProvisioningAddSiteTest", "noRoutes");  
		// else we try to read the options and decide, whether all tests or
		// only a single test is executed:
		
		// create Options object
		Options options = new Options();

		// create the Options
		options.addOption("t", "testcase", true, 
				"(optional) TestCaseName. All test cases will be executed, if this option is missing.");

		CommandLineParser parser = new GnuParser();
		
		try {
		    // parse the command line arguments
		    CommandLine cmd = parser.parse(options, args);

		    // validate that block-size has been set
		    if (cmd.hasOption("testcase")) {
		        // print the value of block-size
		        System.out.println(cmd.getOptionValue("testcase"));
		        Request request = Request.method(Class.forName("de.oveits.simplerestfulfilestorage.SimpleRestfulFileStorageTests"),
		        		cmd.getOptionValue("testcase"));

		        Result result = new JUnitCore().run(request);
		    } else {
		    	JUnitCore.main(
		  	  	      "de.oveits.simplerestfulfilestorage.SimpleRestfulFileStorageTests"); 
		    }     
		}
		
		catch (ParseException exp) {
		    System.out.println("Unexpected exception:" + exp.getMessage());
		 // automatically generate the help statement
		    HelpFormatter formatter = new HelpFormatter();
		    
		    // not tested yet:
		    // get jar path, see http://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
		    String path = MainTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		    String decodedPath = URLDecoder.decode(path, "UTF-8");
		    
		    // get option help text:
		    formatter.printHelp("java -jar " + decodedPath, options);
//		    System.out.println("Available Testcases:" + Class.forName("de.oveits.simplerestfulfilestorage.SimpleRestfulFileStorageTests").getMethods().getClass().getSimpleName());
		    
		}
		
//	    JUnitCore.main(
//	  	      "de.oveits.simplerestfulfilestorage.SimpleRestfulFileStorageTests"); 
	    }

}

