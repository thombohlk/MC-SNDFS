package driver;



import java.io.File;
import java.io.FileNotFoundException;

import java.util.Map;
import java.util.HashMap;

import graph.GraphFactory;
import graph.Graph;
import graph.State;

import ndfs.nndfs.Color;

import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;



public class Main {



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


    private static void runNDFS2(String version, Map<State, ndfs.mcndfs_1_naive.Color> colorStore,
    		File file, int nrOfThreads) throws FileNotFoundException {

//        Graph graph = GraphFactory.createGraph(file);
        NDFS ndfs = NDFSFactory.createMCNDFSNaive(file, colorStore);
        long start = System.currentTimeMillis();
        long end;
        try {
        	ndfs.init(nrOfThreads);
            ndfs.ndfs();
            throw new Error("No result returned by " + version);
        }
        catch (Result r) {
            end = System.currentTimeMillis();
            System.out.println(r.getMessage());
            System.out.printf("%s took %d ms\n", version, end - start);
        }
    }


    private static void dispatch(File file, String version, int nrOfThreads)
            throws ArgumentException, FileNotFoundException {
        if (version.equals("seq")) {
            if (nrOfThreads != 1) {
                throw new ArgumentException("seq can only run with 1 worker");
            }
            Map<State, Color> map = new HashMap<State, Color>();
            runNDFS("seq", map, file);
        }
        else if (version.equals("naive_mc")) {
            Map<State, ndfs.mcndfs_1_naive.Color> colorStore = new HashMap<State, ndfs.mcndfs_1_naive.Color>();
            runNDFS2("naive_mc", colorStore, file, nrOfThreads);
        }
        else {
            throw new ArgumentException("Unkown version: " + version);
        }
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
        }
    }
}
