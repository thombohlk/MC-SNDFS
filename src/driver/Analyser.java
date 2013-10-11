package driver;

import helperClasses.Global;
import helperClasses.StringArray;
import helperClasses.logger.GraphAnalyser;

import java.io.File;
import java.io.FileNotFoundException;

import ndfs.AlgorithmResult;
import ndfs.Result;

/**
 * Class to that can run, parse and print an analysis of one or multiple input combinations.
 * 
 * @author thomas
 *
 */
public class Analyser {

	
	final public static String MODE_CSV = "csv";
	final public static String MODE_CSVP = "csv_performance";
	final public static String MODE_USER = "user";
	final public static String MODE_USERP = "user_performance";
	final public static String MODE_HEARTBEAT = "heartbeat";

	final public static String[] MODES = new String[] { MODE_CSV, MODE_CSVP, MODE_USER, MODE_USERP, MODE_HEARTBEAT };
	final public static String[] USER_MODES = new String[] { MODE_USER, MODE_USERP };
	final public static String[] PERFORMANCE_MODES = new String[] { MODE_USERP, MODE_CSVP };
	
	public static int ANALYSIS_ITERATIONS = 5;

	protected String fileArg;
	protected String versionArg;
	protected String nrOfThreadsArg;

	protected String[] versionsToAnalyse;
	protected File[] filesToAnalyse;
	protected String[] threadNrsToAnalyse;

	
	public Analyser(String fileArg, String version, String nrOfThreads) {
		this.fileArg = fileArg;
		this.versionArg = version;
		this.nrOfThreadsArg = nrOfThreads;

	}

	/**
	 * Parses the arguments given and starts the analysis process.
	 */
	protected void executeAnalysis()
			throws FileNotFoundException, InstantiationException {
		processVersions();
		processNrOfThreads();
		processFiles();
		
		if (Global.MODE.matches(StringArray.implodeArray(PERFORMANCE_MODES, "\\|")))
			ANALYSIS_ITERATIONS = 10;

		executeCombinations();
	}

	/**
	 * Process the files argument.
	 */
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

	/**
	 * Process the version argument.
	 */
	protected void processVersions() {
		if (this.versionArg.equals("all")) {
			this.versionsToAnalyse = Executor.availableVersions;
		} else {
			this.versionsToAnalyse = versionArg.split("\\|");
		}
	}

	/**
	 * Process the nrOfThreads argument.
	 */
	protected void processNrOfThreads() {
		if (this.nrOfThreadsArg.equals("all")) {
			this.threadNrsToAnalyse = Executor.nrOfThreadsOptions;
		} else {
			this.threadNrsToAnalyse = nrOfThreadsArg.split("\\|");
		}
	}

	/**
	 * Goes through all the possible combinations specified by the arguments and executes
	 * a run for each of them.
	 * 
	 * @throws FileNotFoundException
	 * @throws InstantiationException
	 */
	protected void executeCombinations()
			throws FileNotFoundException, InstantiationException {
		// print headers
		if (Global.MODE.equals(MODE_CSVP)) {
			System.out.println(StringArray.implodeArray(Global.CSV_HEADERS, Global.CSV_DELIMITER));
		} else if (Global.MODE.equals(MODE_CSV)) {
			System.out.println(StringArray.implodeArray(Global.CSV_HEADERS, Global.CSV_DELIMITER)
							+ GraphAnalyser.getAnalysisCSVHeaders());
		}

		// perform run for each combination of parameter
		for (File file : this.filesToAnalyse) {
			for (String version : this.versionsToAnalyse) {
				if (version.equals(Executor.MODE_SEQ)) {
					executeCombination(version, file, 1);
				} else {
					for (String nrOfThreads : this.threadNrsToAnalyse) {
						executeCombination(version, file, Integer.valueOf(nrOfThreads));
					}
				}
				if (this.threadNrsToAnalyse.length > 1)
					System.out.println();
			}
			if (this.versionsToAnalyse.length > 1)
				System.out.println();
		}
	}

	/**
	 * Executes either a heartbeat run or a analysis run, depending on the mode.
	 * 
	 * @param version
	 * @param file
	 * @param nrOfThreads
	 * @throws FileNotFoundException
	 * @throws InstantiationException
	 */
	private void executeCombination(String version, File file, int nrOfThreads)
			throws FileNotFoundException, InstantiationException {
		if (Global.MODE.equals(MODE_HEARTBEAT)) {
			AlgorithmResult result = executeRun(0, version, file, nrOfThreads);
			result.setFile(file);
			result.setNrOfThreads(nrOfThreads);
			printAlgorithmResult(result);
		} else {
			executeAnalysisRun(version, file, nrOfThreads);
		}
	}

	/**
	 * Executes an analysis run. This means either 5 or 10 runs are made,
	 * depending on the mode, and an average result is created and presented.
	 * 
	 * @param version
	 * @param file
	 * @param nrOfThreads
	 * @throws FileNotFoundException
	 * @throws InstantiationException
	 */
	protected void executeAnalysisRun(String version, File file, int nrOfThreads)
			throws FileNotFoundException, InstantiationException {
		AlgorithmResult[] results = new AlgorithmResult[ANALYSIS_ITERATIONS];
		AlgorithmResult result;

		if (Global.MODE.matches(StringArray.implodeArray(USER_MODES, "|")))
			System.out.println("Analysing " + version + " with " + nrOfThreads + " threads on " + file.getName() + ".");

		for (int i = 0; i < ANALYSIS_ITERATIONS; i++) {
			results[i] = executeRun(i, version, file, nrOfThreads);
		}
		
		result = constructAverageResult(results, version);
		result.setFile(file);
		result.setNrOfThreads(nrOfThreads);
		printAlgorithmResult(result);
	}

	/**
	 * Executes on run for the given input.
	 * 
	 * @param iteration
	 * @param version
	 * @param file
	 * @param nrOfThreads
	 * @return AlgorithmResult
	 * @throws FileNotFoundException
	 * @throws InstantiationException
	 */
	private AlgorithmResult executeRun(int iteration, String version,
			File file, int nrOfThreads) throws FileNotFoundException,
			InstantiationException {
		AlgorithmResult result = null;
		
		// output progress and set global seed to new value
		if (Global.MODE.matches(StringArray.implodeArray(USER_MODES, "|")))
			System.out.println("Iteration " + (iteration + 1) + "...");
		Global.SEED = Global.SEED_ARRAY[iteration % Global.SEED_ARRAY.length];
		
		// perform a normal run. a log run is executed if needed after which the results are combined
		try {
			Executor.run(version, file, nrOfThreads, "none");
		} catch (AlgorithmResult r) {
			result = r;
		}
		if (! Global.MODE.matches(StringArray.implodeArray(PERFORMANCE_MODES, "|"))) {
			try {
				Executor.run(version, file, nrOfThreads, "log");
			} catch (AlgorithmResult r) {
				r.setDuration(result.getDuration());
				result = r;
			}
		}
		
		return result;
	}

	/**
	 * Constructs an average result, based on the values of the result in results.
	 * 
	 * @param results
	 * @param version
	 * @return
	 */
	private AlgorithmResult constructAverageResult(AlgorithmResult[] results, String version) {
		AlgorithmResult averageResult;
		Result result = checkAndConstructResultMessage(results);
		long averageDuration = calculateAverageDuration(results);
		
		averageResult = new AlgorithmResult(result, averageDuration, version);

		if (! Global.MODE.matches(StringArray.implodeArray(PERFORMANCE_MODES, "|")))
			averageResult.setAnalysisData(GraphAnalyser.constructAverageDataObject(results));
		
		return averageResult;
	}

	/**
	 * Checks if all results are equal and generates an result message accordingly.
	 * 
	 * @param results
	 * @return Result
	 */
	private Result checkAndConstructResultMessage(AlgorithmResult[] results) {
		for (int i = 0; i < results.length; i++) {
			if (!results[i].getResult().isEqualTo(results[0].getResult())) {
				return new Result("not all outputs are the same!");
			}
		}
		return results[0].getResult();
	}

	/**
	 * Calculates average duration for multiple results.
	 * 
	 * @param results
	 * @return long
	 */
	private long calculateAverageDuration(AlgorithmResult[] results) {
		long total = 0;
		long average = 0;
		
		for (int i = 0; i < results.length; i++) {
			total = total + results[i].getDuration();
		}
		average = total / results.length;

		return average;
	}

	/**
	 * Calls the proper print method according to the mode.
	 * 
	 * @param result
	 */
	private void printAlgorithmResult(AlgorithmResult result) {
		switch (Global.MODE) {
		case MODE_USER:
		case MODE_USERP:
			printAlgorithmResultUser(result);
			break;
		case MODE_HEARTBEAT:
			printAlgorithmResultCSV(result);
			printAlgorithmResultHeartBeat(result);
			break;
		case MODE_CSV:
		case MODE_CSVP:
		default:
			printAlgorithmResultCSV(result);
			break;
		}
	}

	/**
	 * Print the results as CSV output.
	 * 
	 * @param result
	 */
	private void printAlgorithmResultCSV(AlgorithmResult result) {
		
		String delimiter = Global.CSV_DELIMITER;
		System.out.print(
				result.getVersion() + delimiter + 
				result.getFileName() + delimiter + 
				result.getNrOfThreads() + delimiter + 
				result.getMessage() + delimiter + 
				result.getDuration() + delimiter +
				(! Global.MODE.equals(MODE_CSVP) ? result.getAnalysisData().getResultsCSV() : "" ) +
				"\n");
	}

	/**
	 * Print the results for a user.
	 * 
	 * @param result
	 */
	private void printAlgorithmResultUser(AlgorithmResult result) {
		System.out.println(result.getVersion() + " took "
				+ result.getDuration() + "ms with: " + result.getMessage()
				+ ".");
		if (! Global.MODE.equals(MODE_USERP))
			System.out.print(result.getAnalysisData().getResultsUser() + "\n");
	}

	/**
	 * Output the results of a heartbeat run.
	 * 
	 * @param result
	 */
	private void printAlgorithmResultHeartBeat(AlgorithmResult result) {
		result.getAnalysisData().printHeartBeats();
	}

}
