package helperClasses.logger;

import graph.Graph;
import graph.State;
import helperClasses.Global;
import helperClasses.StringArray;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ndfs.AlgorithmResult;



public class GraphAnalyser {
	
	public static final String[] ANALYSIS_CSV_HEADERS = new String[] {
			"#states", "#blueVisits", "#redVisits", "#unvisitedBlueStates", "#unvisitedRedStates", "blueOverlapCoefficient", "redOverlapCoefficient" };
	
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
	
	
	public void count() {
		State s = graph.getInitialState();
		processState(s);
	}
	
	
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

	
	public void printResults() {
		System.out.println(data.getResultsUser());
	}
	
	
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


	public static GraphAnalysisDataObject constructAverageDataObject(AlgorithmResult[] results) {
		GraphAnalysisDataObject data = new GraphAnalysisDataObject();
		GraphAnalysisDataObject result = new GraphAnalysisDataObject();
		int nrOfResults = results.length;

		int totalNrOfUnvisitedBlueStates = 0;
		int totalNrOfUnvisitedRedStates = 0;
		int totalNrOfBlueVisists = 0;
		int totalNrOfRedVisists = 0;
		
		for (int i = 0; i < results.length; i++) {
			data = results[i].getAnalysisData();
			totalNrOfUnvisitedBlueStates += data.nrOfUnvisitedBlues;
			totalNrOfUnvisitedRedStates += data.nrOfUnvisitedReds;
			totalNrOfBlueVisists += data.nrOfBlueVisists;
			totalNrOfRedVisists += data.nrOfRedVisists;
		}
		
		result.nrOfStates = (int) results[0].getAnalysisData().nrOfStates;
		result.nrOfUnvisitedBlues = totalNrOfUnvisitedBlueStates / nrOfResults;
		result.nrOfUnvisitedReds = totalNrOfUnvisitedRedStates / nrOfResults;
		result.nrOfBlueVisists = totalNrOfBlueVisists / nrOfResults;
		result.nrOfRedVisists = totalNrOfRedVisists / nrOfResults;
		
		return result;
	}
	
}
