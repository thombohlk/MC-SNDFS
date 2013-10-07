package helperClasses.logger;

import graph.Graph;
import graph.State;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;



public class GraphAnalyser {

	public double totalNrOfStates = 0;
	public int totalNrOfUnvisitedBlueStates = 0;
	public int totalNrOfUnvisitedRedStates = 0;
	public int totalNrOfBlueVisists = 0;
	public int totalNrOfRedVisists = 0;
	
	private HashSet<State> visitedStates;
	private Graph graph;

    private ConcurrentHashMap<Integer, HashSet<State>> stateBlueVisits;
    private ConcurrentHashMap<Integer, HashSet<State>> stateRedVisits;
	
	public GraphAnalyser(Graph graph, ConcurrentHashMap<Integer, HashSet<State>> stateBlueVisits, ConcurrentHashMap<Integer, HashSet<State>> stateRedVisits) {
		this.visitedStates = new HashSet<State>();
		this.graph = graph;
		this.stateBlueVisits = stateBlueVisits;
		this.stateRedVisits = stateRedVisits;
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
		totalNrOfStates++;
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
				totalNrOfBlueVisists++;
				visitedBlue = true;
			}
		}
		if (! visitedBlue) {
			totalNrOfUnvisitedBlueStates++;
		}
		
		for (Integer i : stateRedVisits.keySet()) {
			if (stateRedVisits.get(i).contains(s)) {
				totalNrOfRedVisists++;
				visitedRed = true;
			}
		}
		if (! visitedRed) {
			totalNrOfUnvisitedRedStates++;
		}
	}

	public void printResults() {
		System.out.println(getResultsUser());
	}

	public String getResultsCSV() {
		String result = "";
		result += totalNrOfStates + ";\t";
		result += totalNrOfBlueVisists + ";\t";
		result += totalNrOfRedVisists + ";\t";
		result += totalNrOfUnvisitedBlueStates + ";\t";
		result += totalNrOfUnvisitedRedStates + ";\t";
		result += (totalNrOfBlueVisists / (totalNrOfStates - totalNrOfUnvisitedBlueStates)) + ";\t";
		result += (totalNrOfRedVisists / (totalNrOfStates - totalNrOfUnvisitedRedStates));
		return result;
	}
	
	public static String getCSVHeaders() {
		String result = "#states;\t#blueVisits;\t#redVisits;\t#unvisitedBlueStates;\t#unvisitedRedStates;\tblueOverlapCoefficient;\tredOverlapCoefficient";
		return result;
	}

	public String getResultsUser() {
		String result = "";
		result += ("Total amount of states: " + totalNrOfStates + "\n");
		result += ("Total number of blue visits: " + totalNrOfBlueVisists + "\n");
		result += ("Total number of red visits: " + totalNrOfRedVisists + "\n");
		result += ("Total number of unvisited blue states: " + totalNrOfUnvisitedBlueStates + "\n");
		result += ("Total number of unvisited red states: " + totalNrOfUnvisitedRedStates + "\n");
		result += ("Blue overlap coefficient: " + (totalNrOfBlueVisists / (totalNrOfStates - totalNrOfUnvisitedBlueStates)) + "\n");
		result += ("Red overlap coefficient: " + (totalNrOfRedVisists / (totalNrOfStates - totalNrOfUnvisitedRedStates)) + "\n");
		return result;
	}
	
}
