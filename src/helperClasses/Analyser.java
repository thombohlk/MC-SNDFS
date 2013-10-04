package helperClasses;

import graph.State;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import driver.Executor;
import ndfs.AlgorithmResult;
import ndfs.Result;

public class Analyser {

	public static String[] versions = { "naive", "extended", "lock", "nosync",
			"optimalPermutation2", "optimalPermutation3" };
	
	protected File file;
	protected String version;
	protected int nrOfThreads;
	
	public Analyser() {
		
	}

	public void init(File file, String version, int nrOfThreads) {
		this.file = file;
		this.version = version;
		this.nrOfThreads = nrOfThreads;
	}
	
	public static void makeComparison(File file, int nrOfThreads, int nrOfIterations) throws FileNotFoundException, InstantiationException {
		for (int i = 0; i < versions.length; i++) {
			analyseVersion(versions[i], file, nrOfThreads, nrOfIterations);
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
	private static void runComparisonOnFile(File file, int nrOfThreads,
			int nrOfIterations) throws FileNotFoundException,
			InstantiationException {
		AlgorithmResult[][] results = new AlgorithmResult[versions.length + 1][nrOfIterations];

		for (int i = 0; i < nrOfIterations; i++) {
			System.out.println("Iteration " + (i + 1));
			try {
				System.out.print("Running sequential algorithm... ");
				Executor.runNDFS("seq", new HashMap<State, Color>(), file, "none");
//				runNDFS("seq", new HashMap<State, Color>(), file, "none");
			} catch (AlgorithmResult result) {
				results[0][i] = result;
				System.out.print(result.getDuration() + " ms\n");
			}

			for (int j = 1; j < versions.length + 1; j++) {
				String version = versions[j - 1];
				System.out.print("Running " + version + " algorithm... ");
				try {
					Executor.runMCNDFS(version, file, nrOfThreads, "none");
//					runMCNDFS(version, file, nrOfThreads, "none");
				} catch (AlgorithmResult result) {
					results[j][i] = result;
					System.out.print("\t" + result.getDuration() + " ms\t"
							+ result.getMessage() + "\n");
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
			Long average = total / results[0].length;
			AlgorithmResult ar = new AlgorithmResult(new Result("dummy"),
					average, (i == 0 ? "seq" : versions[i - 1]));
			averages.add(ar);
		}

		Collections.sort(averages);
		for (int i = 0; i < averages.size(); i++) {
			AlgorithmResult result = averages.get(i);
			System.out.println((i + 1) + ": " + result.getVersion() + " at "
					+ result.getDuration() + "ms.");
		}

		for (int i = 0; i < results.length; i++) {
			for (int j = 0; j < results[0].length; j++) {
				if (!results[i][j].getResult().isEqualTo(
						results[i][0].getResult())) {
					System.out.println("Not all outputs are the same for "
							+ (i == 0 ? "seq" : versions[i - 1]) + ".");
					return;
				}
			}
		}
		System.out.println("All outputs are the same!");
	}
	
	/**
	 * Performs all versions of the NDFS algorithm and compares outputs.
	 * 
	 * @param file
	 * @param nrOfThreads
	 * @throws FileNotFoundException
	 * @throws InstantiationException
	 */
	public static void analyseVersion(String version, File file, int nrOfThreads,
			int nrOfIterations) throws FileNotFoundException,
			InstantiationException {
		AlgorithmResult[] results = new AlgorithmResult[nrOfIterations];

		for (int i = 0; i < nrOfIterations; i++) {
			try {
				Executor.runMCNDFS(version, file, nrOfThreads, "log");
			} catch (AlgorithmResult result) {
				results[i] = result;
			}
		}
		
		long average = calculateAverageResult(results);
		AlgorithmResult averageResult = new AlgorithmResult(new Result("All outputs are the same!"), average, version);
		averageResult.printCSVOutput();
//
//		Collections.sort(averages);
//		for (int i = 0; i < averages.size(); i++) {
//			AlgorithmResult result = averages.get(i);
//			System.out.println((i + 1) + ": " + result.getVersion() + " at "
//					+ result.getDuration() + "ms.");
//		}
//
//		for (int i = 0; i < results.length; i++) {
//			for (int j = 0; j < results[0].length; j++) {
//				if (!results[i][j].getResult().isEqualTo(
//						results[i][0].getResult())) {
//					System.out.println("Not all outputs are the same for "
//							+ (i == 0 ? "seq" : versions[i - 1]) + ".");
//					return;
//				}
//			}
//		}
//		System.out.println("All outputs are the same!");
	}

	private static long calculateAverageResult(AlgorithmResult[] results) {
		Long total = (long) 0;
		
		for (int i = 0; i < results.length; i++) {
			total = total + results[i].getDuration();
		}
		Long average = total / results.length;
				
		return average;
	}

}
