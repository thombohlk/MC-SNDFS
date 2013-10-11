package helperClasses.logger;

import graph.Graph;
import graph.State;
import helperClasses.Global;
import helperClasses.Statistics;
import helperClasses.StringArray;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ndfs.AlgorithmResult;



public class GraphAnalyser {
	
	public static final String[] ANALYSIS_CSV_HEADERS = new String[] {
			"#states", "#blueVisits", "#redVisits", "#unvisitedBlueStates", "#unvisitedRedStates", "blueOverlapCoefficient", "redOverlapCoefficient", "ave blue visits", "ave red states", "std blue visits", "std red visits" };
	
	private GraphAnalysisDataObject data;
	private HashSet<State> visitedStates;
	private Graph graph;

    private ConcurrentHashMap<Integer, HashSet<State>> stateBlueVisits;
    private ConcurrentHashMap<Integer, HashSet<State>> stateRedVisits;
	
    
	public GraphAnalyser(GraphAnalysisDataObject data) {
		this.data = data;
		this.visitedStates = new HashSet<State>();
		this.graph = data.graph;
		this.stateBlueVisits = data.stateBlueVisits;
		this.stateRedVisits = data.stateRedVisits;
	}
	
	/**
	 * Start the walk through the tree to analyse the tree.
	 */
	public void analyseOverlap() {
		State s = graph.getInitialState();
		processState(s);
	}
	
	/**
	 * Recursive method for walking through the graph.
	 * @param s
	 */
	private void processState(State s) {
		if (visitedStates.contains(s)) {
			return;
		}
		visitedStates.add(s);
		// count this state
		getData().nrOfStates++;
		countState(s);
		
		// count states in post
		for (State t : graph.post(s)) {
			processState(t);
		}
	}

	/**
	 * Counts the number of dfs blue and dfs red calls for state s.
	 * @param s
	 */
	private void countState(State s) {
		boolean visitedBlue = false;
		boolean visitedRed = false;
		
		for (Integer i : stateBlueVisits.keySet()) {
			if (stateBlueVisits.get(i).contains(s)) {
				getData().nrOfBlueVisists++;
				visitedBlue = true;
			}
		}
		if (! visitedBlue) {
			getData().nrOfUnvisitedBlues++;
		}
		
		for (Integer i : stateRedVisits.keySet()) {
			if (stateRedVisits.get(i).contains(s)) {
				getData().nrOfRedVisists++;
				visitedRed = true;
			}
		}
		if (! visitedRed) {
			getData().nrOfUnvisitedReds++;
		}
	}

	/**
	 * Prints the results in a user friendly manner.
	 */
	public void printResults() {
		System.out.println(data.getResultsUser());
	}
	
	/**
	 * Returns the csv headers for analysis parameters.
	 * @return
	 */
	public static String getAnalysisCSVHeaders() {
		String result = StringArray.implodeArray(ANALYSIS_CSV_HEADERS, Global.CSV_DELIMITER);
		return result;
	}

	
	public GraphAnalysisDataObject getData() {
		return data;
	}

	
	public void setData(GraphAnalysisDataObject data) {
		this.data = data;
	}

	/**
	 * Calculates averages and standard deviation for dfs blue and dfs red visits.
	 */
	public void analyseAverage() {
		analyseAverageBlue();
		analyseAverageRed();
	}

	/**
	 * Calculates the average number of dfs red calls per thread and the
	 * standard deviation.
	 */
	private void analyseAverageRed() {
		double[] results = new double[stateRedVisits.keySet().size()];
		int j = 0;

		for (Integer i : stateRedVisits.keySet()) {
			results[j] = stateRedVisits.get(i).size();
			j++;
		}
		
		Statistics stats = new Statistics(results);
		data.aveNrOfRedNodes = stats.getMean();
		data.redNodeStdDev = stats.getStdDev();
	}

	/**
	 * Calculates the average number of dfs blue calls per thread and the
	 * standard deviation.
	 */
	private void analyseAverageBlue() {
		double[] results = new double[stateBlueVisits.keySet().size()];
		int j = 0;
		
		for (Integer i : stateBlueVisits.keySet()) {
			results[j] = stateBlueVisits.get(i).size();
			j++;
		}
		
		Statistics stats = new Statistics(results);
		data.aveNrOfBlueNodes = stats.getMean();
		data.blueNodeStdDev = stats.getStdDev();
	}

	/**
	 * Creates an average data objects from the data of the given algorithm results.
	 * @param results
	 * @return
	 */
	public static GraphAnalysisDataObject constructAverageDataObject(AlgorithmResult[] results) {
		GraphAnalysisDataObject data = new GraphAnalysisDataObject();
		GraphAnalysisDataObject result = new GraphAnalysisDataObject();
		int nrOfResults = results.length;

		int totalNrOfUnvisitedBlueStates = 0;
		int totalNrOfUnvisitedRedStates = 0;
		int totalNrOfBlueVisists = 0;
		int totalNrOfRedVisists = 0;
		double totalAverageBlueVisits = 0;
		double totalAverageRedVisits= 0;
		double totalBlueStdVar = 0;
		double totalRedStdVar = 0;
		
		for (int i = 0; i < results.length; i++) {
			data = results[i].getAnalysisData();
			totalNrOfUnvisitedBlueStates += data.nrOfUnvisitedBlues;
			totalNrOfUnvisitedRedStates += data.nrOfUnvisitedReds;
			totalNrOfBlueVisists += data.nrOfBlueVisists;
			totalNrOfRedVisists += data.nrOfRedVisists;
			totalAverageBlueVisits += data.aveNrOfBlueNodes;
			totalAverageRedVisits += data.aveNrOfRedNodes;
			totalBlueStdVar += data.blueNodeStdDev;
			totalRedStdVar += data.redNodeStdDev;
		}
		
		result.nrOfStates = (int) results[0].getAnalysisData().nrOfStates;
		result.nrOfUnvisitedBlues = totalNrOfUnvisitedBlueStates / nrOfResults;
		result.nrOfUnvisitedReds = totalNrOfUnvisitedRedStates / nrOfResults;
		result.nrOfBlueVisists = totalNrOfBlueVisists / nrOfResults;
		result.nrOfRedVisists = totalNrOfRedVisists / nrOfResults;
		result.aveNrOfBlueNodes = totalAverageBlueVisits / nrOfResults;
		result.aveNrOfRedNodes = totalAverageRedVisits / nrOfResults;
		result.blueNodeStdDev = totalBlueStdVar / nrOfResults;
		result.redNodeStdDev = totalRedStdVar / nrOfResults;
		
		return result;
	}
	
}
