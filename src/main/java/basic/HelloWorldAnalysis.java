package basic;

import soot.*;
import soot.options.Options;
import java.util.*;

public class HelloWorldAnalysis {
    public static void main(String[] args) {
        // Initialize Soot
        G.reset();

        // Set Soot options
        Options.v().set_whole_program(true);// Set whole program analysis
        Options.v().set_allow_phantom_refs(true);// Allow phantom references
        Options.v().set_prepend_classpath(true);// Prepend classpath
        Options.v().set_src_prec(Options.src_prec_class);// Set source precision
        Options.v().set_output_format(Options.output_format_jimple);// Set output format
        Options.v().set_process_dir(Collections.singletonList("target/classes"));// Set process directory

        //load the class to be analyzed
        Scene.v().loadNecessaryClasses();
        SootClass sootClass = Scene.v().loadClassAndSupport("HelloWorld");
        sootClass.setApplicationClass();// Set application class

        // Perform the analysis
        PackManager.v().runPacks();// Run packs
        Options.v().set_output_dir("SootOutput");// Set output directory
        PackManager.v().writeOutput();// Write output

        // Print the results
        System.out.println("Analysis complete");



        // Print the analysis result
        for (SootMethod method : sootClass.getMethods()) {
            //print sootCLASS
            System.out.println("SootClass: " + sootClass.getName());
            if (method.hasActiveBody()) {
                System.out.println(method.retrieveActiveBody());
            } else {
                System.out.println("No active body for method: " + method.getName());
            }
        }
    }
}
