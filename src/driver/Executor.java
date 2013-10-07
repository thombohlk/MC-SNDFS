package driver;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
import helperClasses.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import mcndfs.MCNDFS;
import ndfs.AlgorithmResult;
import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;

public class Executor {

	public Executor() {

	}
	
	public static void run(String version, File file, int nrOfThreads,
			String loggingMode) throws FileNotFoundException, InstantiationException, AlgorithmResult {
		if (version.equals("seq")) {
			runNDFS(version, file, loggingMode);
		} else {
			runMCNDFS(version, file, nrOfThreads, loggingMode);
		}
	}

	public static void runNDFS(String version, File file, String loggingMode)
			throws FileNotFoundException, AlgorithmResult {
		boolean useLogging = (loggingMode.equals("log") ? true : false);

		Map<State, Color> colorStore = new HashMap<State, Color>();
		Graph graph = GraphFactory.createGraph(file);
		NDFS ndfs = NDFSFactory.createNNDFS(graph, colorStore, useLogging);
		long start = System.currentTimeMillis();
		long end;
		try {
			ndfs.ndfs();
			throw new Error("No result returned by " + version);
		} catch (Result r) {
			end = System.currentTimeMillis();
			throw new AlgorithmResult(r, end - start, version);
		} finally {
			ndfs.tearDown();
		}
	}

	public static void runMCNDFS(String version, File file, int nrOfThreads,
			String loggingMode) throws FileNotFoundException,
			InstantiationException, AlgorithmResult {
		boolean useLogging = (loggingMode.equals("log") ? true : false);

		MCNDFS mcndfs = NDFSFactory.createMCNDFS(version, file, useLogging);
		mcndfs.init(nrOfThreads);
		long start = System.currentTimeMillis();
		long end;
		try {
			mcndfs.ndfs();
			throw new Error("No result returned by " + version);
		} catch (Result r) {
			end = System.currentTimeMillis();
			throw new AlgorithmResult(r, end - start, version);
		} finally {
			mcndfs.tearDown();
		}
	}

}
