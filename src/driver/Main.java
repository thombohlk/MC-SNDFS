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

	private static String[] versions = { "optimalPermutation", "optimalPermutation2", "optimalPermutation3"};

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
            File file) throws FileNotFoundException, AlgorithmResult {

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
            throw new AlgorithmResult(r, end - start, version);
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
            throw new AlgorithmResult(r, end - start, version);
        }
    }


    private static void dispatch(File file, String version, int nrOfThreads, int nrOfIterations)
            throws ArgumentException, FileNotFoundException, InstantiationException {
        if (version.matches("compare")) {
            runComparison(file, nrOfThreads, nrOfIterations);
        } else {
	        try {
		        if (version.equals("seq")) {
		            if (nrOfThreads != 1) {
		                throw new ArgumentException("seq can only run with 1 worker");
		            }
		            Map<State, Color> map = new HashMap<State, Color>();
						runNDFS("seq", map, file);
		        }
		        else if (version.matches("naive|extended|optimalPermutation|optimalPermutation2|optimalPermutation3|lock|nosync")) {
					runMCNDFS(version, file, nrOfThreads);
		        }
		        else {
		            throw new ArgumentException("Unkown version: " + version);
		        }
			} catch (AlgorithmResult r) {
	            System.out.println(r.getMessage());
	            System.out.printf("%s took %d ms\n", r.getVersion(), r.getDuration());
			}
        }
    }
    
    private static void runComparison (File file, int nrOfThreads, int nrOfIterations) throws FileNotFoundException, InstantiationException {
    	if (file.getName().equals("all")) {
    		// TODO
    	} else {
    		runComparisonOnFile(file, nrOfThreads, nrOfIterations);
    	}
    }

    /**
     * Performs all versions of the NDFS algorithm and compares outputs.
     * 
     * @param file
     * @param nrOfThreads
     * @throws FileNotFoundException 
     * @throws InstantiationException 
     */
    private static void runComparisonOnFile(File file, int nrOfThreads, int nrOfIterations) throws FileNotFoundException, InstantiationException {
        AlgorithmResult[][] results = new AlgorithmResult[versions.length + 1][nrOfIterations];

        for (int i = 0; i < nrOfIterations; i++) {
    		System.out.println("Iteration " + (i+1));
	        try {
	    		System.out.print("Running sequential algorithm... ");
				runNDFS("seq", new HashMap<State, Color>(), file);
			} catch (AlgorithmResult result) {
				results[0][i] = result;
	    		System.out.print(result.getDuration() + " ms\n");
			}
	
	        for (int j = 1; j < versions.length + 1; j++) {
	        	String version = versions[j-1];
	    		System.out.print("Running " + version + " algorithm... ");
	            try {
					runMCNDFS(version, file, nrOfThreads);
				} catch (AlgorithmResult result) {
					results[j][i] = result;
		    		System.out.print("\t" + result.getDuration() + " ms\t" + result.getMessage() + "\n");
				}
	        }
    		System.out.println("");
        }

        ArrayList<AlgorithmResult> averages = new ArrayList<AlgorithmResult>();
        for (int i = 0; i < results.length; i++) {
        	Long total = (long) 0;
	        for (int j = 0; j < results[0].length; j++) {
	        	total = total + results[i][j].getDuration();
	        }
	        Long average = total/results[0].length;
	        AlgorithmResult ar = new AlgorithmResult(new Result("dummy"), average, (i == 0 ? "seq" : versions[i-1]));
	        averages.add(ar);
        }
        
        Collections.sort(averages);
        for (int i = 0; i < averages.size(); i++) {
        	AlgorithmResult result = averages.get(i);
    		System.out.println((i+1) + ": " + result.getVersion() + " at " + result.getDuration() + "ms.");
        }

		for (int i = 0; i < results.length; i++) {
			for (int j = 0; j < results[0].length; j++) {
		    	if (! results[i][j].getResult().isEqualTo(results[i][0].getResult()) ) {
		    		System.out.println("Not all outputs are the same for " + (i == 0 ? "seq" : versions[i-1]) + ".");
		        	return;
		    	}
			}
        }
		System.out.println("All outputs are the same!");
	}


	public static void main(String[] argv) {
        try {
        	int nrOfiterations = 1;
            if (argv.length < 3 || argv.length > 4) 
                throw new ArgumentException("Wrong number of arguments");
            if (argv.length == 4) {
            	try { 
            		nrOfiterations = Integer.parseInt(argv[3]);
                } catch(NumberFormatException e) { 
                    throw new ArgumentException("Fourth argument should be a number");
                }
            }
            File file = new File(argv[0]);
            String version = argv[1];
            int nrOfThreads = new Integer(argv[2]);
            
            dispatch(file, version, nrOfThreads, nrOfiterations);
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
