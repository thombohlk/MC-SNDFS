package driver;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import graph.GraphFactory;
import graph.Graph;
import graph.State;
import helperClasses.Color;
import ndfs.AlgorithmResult;
import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;



public class Main {

	private static String[] versions = { "naive", "extended", "optimalPermutation" };

    private static class ArgumentException extends Exception {
        private static final long serialVersionUID = 1L;
        ArgumentException(String message) {
            super(message);
        }
    };


    private static void printUsage() {
        System.out.println("Usage: bin/ndfs <file> <version> <nrWorkers>");
        System.out.println("  where"); 
        System.out.println("    <file> is a Promela file (.prom)");
        System.out.println("    <version> is one of {seq}");
    }


    private static void runNDFS(String version, Map<State, Color> colorStore,
            File file) throws FileNotFoundException {

        Graph graph = GraphFactory.createGraph(file);
        NDFS ndfs = NDFSFactory.createNNDFS(graph, colorStore);
        long start = System.currentTimeMillis();
        long end;
        try {
            ndfs.ndfs();
            throw new Error("No result returned by " + version);
        }
        catch (Result r) {
            end = System.currentTimeMillis();
            System.out.println(r.getMessage());
            System.out.printf("%s took %d ms\n", version, end - start);
        }
    }


    private static void runMCNDFS(String version, File file,
    		int nrOfThreads) throws FileNotFoundException, InstantiationException, AlgorithmResult {

        NDFS ndfs = NDFSFactory.createMCNDFS(version, file);
    	ndfs.init(nrOfThreads);
        long start = System.currentTimeMillis();
        long end;
        try {
            ndfs.ndfs();
            throw new Error("No result returned by " + version);
        }
        catch (Result r) {
            end = System.currentTimeMillis();
            System.out.println(r.getMessage());
            System.out.printf("%s took %d ms\n", version, end - start);
            
            throw new AlgorithmResult("done", end - start, r);
        }
    }


    private static void dispatch(File file, String version, int nrOfThreads)
            throws ArgumentException, FileNotFoundException, InstantiationException {
        if (version.equals("seq")) {
            if (nrOfThreads != 1) {
                throw new ArgumentException("seq can only run with 1 worker");
            }
            Map<State, Color> map = new HashMap<State, Color>();
            runNDFS("seq", map, file);
        }
        else if (version.matches("naive|extended|optimalPermutation")) {
            try {
				runMCNDFS(version, file, nrOfThreads);
			} catch (AlgorithmResult e) {
				
			}
        }
        else if (version.matches("compare")) {
            runComparison(file, nrOfThreads);
        }
        else {
            throw new ArgumentException("Unkown version: " + version);
        }
    }

    /**
     * Performes all versions of the NDFS algorithm and compares outputs.
     * 
     * @param file
     * @param nrOfThreads
     * @throws FileNotFoundException 
     * @throws InstantiationException 
     */
    private static void runComparison(File file, int nrOfThreads) throws FileNotFoundException, InstantiationException {
		System.out.println("Running sequential algorithm...");
        runNDFS("seq", new HashMap<State, Color>(), file);

        ArrayList<Long> durations = new ArrayList<Long>();
        ArrayList<Result> results = new ArrayList<Result>();
        
        long fastest = Long.MAX_VALUE;
        String fastestS = "";

        for (String version : versions) {
    		System.out.println("Running " + version + " algorithm...");
            try {
				runMCNDFS(version, file, nrOfThreads);
			} catch (AlgorithmResult e) {
				durations.add(e.getDuration());
				results.add(e.getResult());
				
				if (e.getDuration() < fastest) {
					fastest = e.getDuration();
					fastestS = version;
				}
			}
        }

		System.out.println("");
		System.out.println(fastestS + " was the fastest algorithm.");
		
        for (Result result : results) {
        	if (! result.compare(results.get(0)) ) {
        		System.out.println("Not all outputs are the same!");
            	return;
        	}
        }
		System.out.println("All outputs are the same!");
	}


	public static void main(String[] argv) {
        try {
            if (argv.length != 3) 
                throw new ArgumentException("Wrong number of arguments");
            File file = new File(argv[0]);
            String version = argv[1];
            int nrOfThreads = new Integer(argv[2]);

            dispatch(file, version, nrOfThreads);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        catch (ArgumentException e) {
            System.err.println(e.getMessage());
            printUsage();
        }
        catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            printUsage();
        } catch (InstantiationException e) {
            System.err.println(e.getMessage());
            printUsage();
		}
    }
}
