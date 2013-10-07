package driver;

import helperClasses.Global;
import helperClasses.logger.GraphAnalyser;
import helperClasses.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import ndfs.AlgorithmResult;
import ndfs.Result;

public class Analyser {

	public static String[] availableVersions = new String[] { "seq", "naive",
			"extended", "lock", "nosync", "optimalPermutation2", "optimalPermutation3" };
	public static String[] nrOfThreadsOptions = new String[] { "1", "2", "4",
			"8", "16", "32" };

	protected String fileArg;
	protected String versionArg;
	protected String nrOfThreadsArg;
	protected String outputTypeArg;

	protected String[] versionsToAnalyse;
	protected File[] filesToAnalyse;
	protected String[] threadNrToAnalyse;

	public Analyser() {

	}

	public void init(String fileArg, String version, String nrOfThreads,
			String outputType) {
		this.fileArg = fileArg;
		this.versionArg = version;
		this.nrOfThreadsArg = nrOfThreads;
		this.outputTypeArg = outputType;
	}

	protected void makeComparison()
			throws FileNotFoundException, InstantiationException {
		processVersions();
		processNrOfThreads();
		processFiles();

		startAnalysis();
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

	protected void startAnalysis()
			throws FileNotFoundException, InstantiationException {
		if (this.outputTypeArg.equals("csv_performance")) {
			System.out.println("version;\tfile;\tnrOfThreads;\tresult;\tduration;\t");
		} else if (this.outputTypeArg.equals("csv")) {
			System.out.println("version;\tfile;\tnrOfThreads;\tresult;\tduration;\t"
							+ GraphAnalyser.getCSVHeaders());
		}

		for (File file : this.filesToAnalyse) {
			for (String version : this.versionsToAnalyse) {
				if (version.equals("seq")) {
					analyseVersion(version, file, 1);
				} else {
					for (String nrOfThreads : this.threadNrToAnalyse) {
						analyseVersion(version, file, Integer.valueOf(nrOfThreads));
					}
				}
				if (this.threadNrToAnalyse.length > 1)
					System.out.println();
			}
			if (this.versionsToAnalyse.length > 1)
				System.out.println();
		}
	}

	protected void analyseVersion(String version, File file, int nrOfThreads)
			throws FileNotFoundException, InstantiationException {
		AlgorithmResult[] results = new AlgorithmResult[Global.ANALYSIS_ITERATIONS];

		if (this.outputTypeArg.matches("user|user_performance"))
			System.out.println("Analysing " + version + " with " + nrOfThreads
					+ " threads on " + file.getName() + ".");
		if (version.equals("seq"))
			nrOfThreads = 1;

		for (int i = 0; i < Global.ANALYSIS_ITERATIONS; i++) {
			if (this.outputTypeArg.matches("user|user_performance"))
				System.out.println("Iteration " + (i + 1) + "...");
			Global.SEED = Global.SEED_ARRAY[i % Global.SEED_ARRAY.length];
			
			try {
				Executor.run(version, file, nrOfThreads, "none");
			} catch (AlgorithmResult result) {
				results[i] = result;
			}
			
			if (! this.outputTypeArg.matches("csv_performance|user_performance")) {
				try {
					Executor.run(version, file, nrOfThreads, "log");
				} catch (AlgorithmResult result) {
					result.setDuration(results[i].getDuration());
					results[i] = result;
				}
			}
		}

		long averageDuration = calculateAverageDuration(results);
		Result result = checkResultMessages(results);
		AlgorithmResult averageResult = new AlgorithmResult(result,
				averageDuration, version);

		if (! this.outputTypeArg.matches("csv_performance|user_performance"))
			averageResult.setLogger(Logger.calculateAverageLogger(results));

		printAlgorithmResult(version, file, nrOfThreads, averageResult);
	}

	private Result checkResultMessages(AlgorithmResult[] results) {
		for (int i = 0; i < results.length; i++) {
			if (!results[i].getResult().isEqualTo(results[0].getResult())) {
				return new Result("not all outputs are the same!");
			}
		}
		return results[0].getResult();
	}

	private static long calculateAverageDuration(AlgorithmResult[] results) {
		// average duration
		long total = 0;
		for (int i = 0; i < results.length; i++) {
			total = total + results[i].getDuration();
		}
		long average = total / results.length;

		return average;
	}

	private void printAlgorithmResult(String version, File file,
			int nrOfThreads, AlgorithmResult result) {
		switch (this.outputTypeArg) {
		case "user":
		case "user_performance":
			printAlgorithmResultUser(result);
			break;
		case "csv":
		case "csv_performance":
			printAlgorithmResultCSV(version, file, nrOfThreads, result);
			break;

		default:
			break;
		}
	}

	private void printAlgorithmResultCSV(String version, File file,
			int nrOfThreads, AlgorithmResult result) {
		System.out.print(version + ";\t" + file.getName() + ";\t" + nrOfThreads
				+ ";\t" + result.getMessage() + ";\t" + result.getDuration()
				+ ";\t");
		if (!this.outputTypeArg.equals("csv_performance"))
			System.out.print(result.getLogger().getResultsCSV());
		System.out.println();
	}

	private void printAlgorithmResultUser(AlgorithmResult result) {
		System.out.println(result.getVersion() + " took "
				+ result.getDuration() + "ms with: " + result.getMessage()
				+ ".");
		if (!this.outputTypeArg.equals("user_performance"))
			System.out.print(result.getLogger().getResultsUser());
		System.out.println();
	}


}
