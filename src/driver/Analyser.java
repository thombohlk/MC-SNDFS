package driver;

import graph.State;
import helperClasses.Color;
import helperClasses.Global;
import helperClasses.logger.GraphAnalyser;
import helperClasses.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ndfs.AlgorithmResult;
import ndfs.Result;

public class Analyser {

	public static String[] availableVersions = new String[] { "naive", "extended", "lock", "nosync",
			"optimalPermutation2", "optimalPermutation3" };
	public static String[] nrOfThreadsOptions = new String[] {"1", "2", "4", "8", "16", "32"};
	
	protected String fileArg;
	protected String versionArg;
	protected String nrOfThreadsArg;
	protected String outputTypeArg;
	
	protected int nrOfIterations;
	protected String[] versionsToAnalyse;
	protected File[] filesToAnalyse;
	protected String[] threadNrToAnalyse;
	
	public Analyser() {
		
	}

	public void init(String fileArg, String version, String nrOfThreads, String outputType) {
		this.fileArg = fileArg;
		this.versionArg = version;
		this.nrOfThreadsArg = nrOfThreads;
		this.outputTypeArg = outputType;
	}
	
	protected void makeComparison(int nrOfIterations) throws FileNotFoundException, InstantiationException {
		this.nrOfIterations = nrOfIterations;
//		if (this.outputType.equals("CSV")) 
//			System.out.println("version, " + "duration, " + GraphAnalyser.getCSVHeaders());
		processVersions();
		processNrOfThreads();
		processFiles();
		
		startAnalysis(nrOfIterations);
	}

	private void processFiles() {
		if (this.fileArg.equals("all")) {
			File folder = new File("input");
			filesToAnalyse = folder.listFiles();
		} else {
			String fileNames[] = fileArg.split("\\|");
			filesToAnalyse = new File[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				File file = new File(fileNames[i]);
				filesToAnalyse[i] = file;
			}
		}
	}

	protected void processVersions() {
		if (this.versionArg.equals("all")) {
			this.versionsToAnalyse = availableVersions;
		} else {
			this.versionsToAnalyse = versionArg.split("\\|");
		}
	}

	protected void processNrOfThreads() {
		if (this.nrOfThreadsArg.equals("all")) {
			this.threadNrToAnalyse = nrOfThreadsOptions;
		} else {
			this.threadNrToAnalyse = nrOfThreadsArg.split("\\|");
			
		}
	}
	
	protected void startAnalysis(int nrOfIterations) throws FileNotFoundException, InstantiationException {
		for (String version : this.versionsToAnalyse) {
			for (File file : this.filesToAnalyse) {
				for (String nrOfThreads : this.threadNrToAnalyse) {
					analyseVersion(version, file, Integer.valueOf(nrOfThreads), nrOfIterations);
				}
			}
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
	protected void analyseVersion(String version, File file, int nrOfThreads, int nrOfIterations) throws FileNotFoundException,
			InstantiationException {
		AlgorithmResult[] results = new AlgorithmResult[nrOfIterations];

		if (this.outputTypeArg.equals("user")) {
			System.out.println("Analysing " + version + " with " + nrOfThreads + " threads on " + file.getName() + ".");
		}
		for (int i = 0; i < nrOfIterations; i++) {
			if (this.outputTypeArg.equals("user")) System.out.println("Iteration " + (i+1) + "...");
			Global.SEED = Global.SEED_ARRAY[i%Global.SEED_ARRAY.length];
			try {
				if (version.equals("seq"))
					nrOfThreads = 1;
				Executor.run(version, file, nrOfThreads, "log");
			} catch (AlgorithmResult result) {
				results[i] = result;
			}
			try {
				if (version.equals("seq"))
					nrOfThreads = 1;
				Executor.run(version, file, nrOfThreads, "none");
			} catch (AlgorithmResult result) {
				results[i].setDuration(result.getDuration());
			}
		}
		
		long average = calculateAverageResult(results);
		AlgorithmResult averageResult = new AlgorithmResult(new Result("All outputs are the same!"), average, version);
		averageResult.setLogger(Logger.calculateAverageLogger(results));
		
		printAlgorithmResult(version, file, nrOfThreads, averageResult);
	}

	private static long calculateAverageResult(AlgorithmResult[] results) {
		// average duration
		long total = 0;
		for (int i = 0; i < results.length; i++) {
			total = total + results[i].getDuration();
		}
		long average = total / results.length;
		
		return average;
	}

	private void printAlgorithmResult(String version, File file, int nrOfThreads, AlgorithmResult result) {
		switch (this.outputTypeArg) {
		case "user":
			printAlgorithmResultUser(result);
			break;
		case "CSV":
			printAlgorithmResultCSV(version, file, nrOfThreads, result);
			break;

		default:
			break;
		}
	}

	private void printAlgorithmResultCSV(String version, File file, int nrOfThreads, AlgorithmResult result) {
		System.out.print(result.getVersion() + ", " + version + ", " + file.getName() + ", " + nrOfThreads + ", " + result.getDuration() + ", ");
		System.out.print(result.getLogger().getResultsCSV());
		System.out.println();
	}

	private void printAlgorithmResultUser(AlgorithmResult result) {
		System.out.println(result.getVersion() + " took " + result.getDuration() + "ms.");
		System.out.print(result.getLogger().getResultsUser());
		System.out.println();
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
		AlgorithmResult[][] results = new AlgorithmResult[availableVersions.length + 1][nrOfIterations];

		for (int i = 0; i < nrOfIterations; i++) {
			System.out.println("Iteration " + (i + 1));
			try {
				System.out.print("Running sequential algorithm... ");
				Executor.runNDFS("seq", file, "none");
//				runNDFS("seq", new HashMap<State, Color>(), file, "none");
			} catch (AlgorithmResult result) {
				results[0][i] = result;
				System.out.print(result.getDuration() + " ms\n");
			}

			for (int j = 1; j < availableVersions.length + 1; j++) {
				String version = availableVersions[j - 1];
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
					average, (i == 0 ? "seq" : availableVersions[i - 1]));
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
							+ (i == 0 ? "seq" : availableVersions[i - 1]) + ".");
					return;
				}
			}
		}
		System.out.println("All outputs are the same!");
	}

}
