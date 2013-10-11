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

/**
 * Class to execute the algorithms.
 * 
 * @author thomas
 *
 */
public class Executor {

	
	final public static String MODE_SEQ = "seq";
	final public static String MODE_NAIVE = "naive";
	final public static String MODE_EXTENDED = "extended";
	final public static String MODE_LOCK = "lock";
	final public static String MODE_NOSYNC = "nosync";
	final public static String MODE_OPTPERM = "optPerm";
	final public static String MODE_LOCALRED = "localRed";

	public static String[] availableVersions = new String[] { MODE_SEQ, MODE_NAIVE,
		MODE_EXTENDED, MODE_LOCK, MODE_NOSYNC, MODE_OPTPERM, MODE_LOCALRED };
	public static String[] nrOfThreadsOptions = new String[] { "1", "2", "4",
		"8", "12", "16", "20", "26", "32", "40", "48" };

	
	public Executor() {	}
	
	/**
	 * Runs either the sequential or one of the multithreaded algorithms,
	 * depending on the version. Throws an AlgorithmResult with data about the run.
	 * 
	 * @param version
	 * @param file
	 * @param nrOfThreads
	 * @param loggingMode
	 * @throws FileNotFoundException
	 * @throws InstantiationException
	 * @throws AlgorithmResult
	 */
	public static void run(String version, File file, int nrOfThreads,
			String loggingMode) throws FileNotFoundException, InstantiationException, AlgorithmResult {
		if (version.equals(MODE_SEQ)) {
			runNDFS(version, file, loggingMode);
		} else {
			runMCNDFS(version, file, nrOfThreads, loggingMode);
		}
	}

	/**
	 * Runs the NDFS for the sequential algorithm. Throws an AlgorithmResult with 
	 * data about the run.
	 * 
	 * @param version
	 * @param file
	 * @param loggingMode
	 * @throws FileNotFoundException
	 * @throws AlgorithmResult
	 */
	public static void runNDFS(String version, File file, String loggingMode)
			throws FileNotFoundException, AlgorithmResult {
		boolean useLogging = (loggingMode.equals("log") ? true : false);
		long start, end;

		Map<State, Color> colorStore = new HashMap<State, Color>();
		Graph graph = GraphFactory.createGraph(file);
		NDFS ndfs = NDFSFactory.createNNDFS(graph, colorStore, useLogging);
		
		start = System.currentTimeMillis();
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

	/**
	 * Runs the MCNDFS for the multithreaded versions of the algorithms. Throws an
	 * AlgorithmResult with data about the run.
	 * 
	 * @param version
	 * @param file
	 * @param nrOfThreads
	 * @param loggingMode
	 * @throws FileNotFoundException
	 * @throws InstantiationException
	 * @throws AlgorithmResult
	 */
	public static void runMCNDFS(String version, File file, int nrOfThreads,
			String loggingMode) throws FileNotFoundException,
			InstantiationException, AlgorithmResult {
		boolean useLogging = (loggingMode.equals("log") ? true : false);
		long start, end;

		MCNDFS mcndfs = NDFSFactory.createMCNDFS(version, file, useLogging);
		mcndfs.init(nrOfThreads);

		start = System.currentTimeMillis();
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
