package nl.eu.clarin.docker.scanner;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class Main {
    public static void main(String[] args) throws Exception {
        new Main().processArguments(args);
    }
    
    private final Logger logger = LoggerFactory.getLogger(Main.class);
     
    public final static String OPT_HELP = "help";
    public final static String OPT_INPUT = "input";
    
    private void processArguments(String[] args) throws ParseException {
        Options options = 
            new Options()
                .addOption(OPT_HELP, false, "print this message" )
                .addOption(OPT_INPUT, true, "input directory");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse( options, args );

        if(line.hasOption(OPT_INPUT)) {
            String path = line.getOptionValue(OPT_INPUT);
            if(path == null) {
                showHelp(options, true);
            } else { 
                new DockerScanner()
                    .scan(new File(path))
                    .filter()
                    .analyze();
            }
        } else {            
            showHelp(options, !line.hasOption(OPT_HELP));
        }   
    }
    
    private void showHelp(Options options, boolean invalid) {
        if(invalid) {
            logger.info("Invalid options");
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar <jarfile>", options );
    }
}
