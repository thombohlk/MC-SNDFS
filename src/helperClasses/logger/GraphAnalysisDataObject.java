package helperClasses.logger;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import graph.Graph;
import graph.State;
import helperClasses.ConcurrentLongHashMap;
import helperClasses.Global;

/**
 * Class for storing and printing data about a graph.
 * @author thomas
 *
 */
public class GraphAnalysisDataObject {

	public long start, end;
	public double nrOfStates = 0;
	public int nrOfUnvisitedBlues = 0;
	public int nrOfUnvisitedReds = 0;
	public int nrOfBlueVisists = 0;
	public int nrOfRedVisists = 0;
	
	public Graph graph;
    public ConcurrentHashMap<Integer, HashSet<State>> stateBlueVisits;
    public ConcurrentHashMap<Integer, HashSet<State>> stateRedVisits;

    public ConcurrentLongHashMap<Long> dfsBlueCount;
    public ConcurrentLongHashMap<Long> dfsBlueStartCount;
    public ConcurrentLongHashMap<Long> dfsBlueDoneCount;
    public ConcurrentLongHashMap<Long> dfsRedCount;
    public ConcurrentLongHashMap<Long> dfsRedStartCount;
    public ConcurrentLongHashMap<Long> dfsRedDoneCount;
    public ConcurrentLongHashMap<Long> waitCount;

    public AtomicLong dfsBlueCounter;
    public AtomicLong dfsBlueStartCounter;
    public AtomicLong dfsBlueDoneCounter;
    public AtomicLong dfsRedCounter;
    public AtomicLong dfsRedStartCounter;
    public AtomicLong dfsRedDoneCounter;
    public AtomicLong waitCounter;

    public double aveNrOfBlueNodes;
    public double aveNrOfRedNodes;
    public double blueNodeStdDev;
    public double redNodeStdDev;

    
	public GraphAnalysisDataObject() {}

	public GraphAnalysisDataObject(Graph graph) {
		this.graph = graph;
		
		initLogVariables();
	}

	private void initLogVariables() {
    	stateBlueVisits = new ConcurrentHashMap<Integer, HashSet<State>>();
    	stateRedVisits = new ConcurrentHashMap<Integer, HashSet<State>>();
    	
    	dfsBlueCount = new ConcurrentLongHashMap<Long>(-1);
    	dfsBlueStartCount = new ConcurrentLongHashMap<Long>(-1);
    	dfsBlueDoneCount = new ConcurrentLongHashMap<Long>(-1);
    	dfsRedCount = new ConcurrentLongHashMap<Long>(-1);
    	dfsRedStartCount = new ConcurrentLongHashMap<Long>(-1);
    	dfsRedDoneCount = new ConcurrentLongHashMap<Long>(-1);
    	waitCount = new ConcurrentLongHashMap<Long>(-1);

    	dfsBlueCounter = new AtomicLong(0);
    	dfsBlueStartCounter = new AtomicLong(0);
    	dfsBlueDoneCounter = new AtomicLong(0);
    	dfsRedCounter = new AtomicLong(0);
    	dfsRedStartCounter = new AtomicLong(0);
    	dfsRedDoneCounter = new AtomicLong(0);
    	waitCounter = new AtomicLong(0);
	}

	public String getResultsCSV() {
		String delimiter = Global.CSV_DELIMITER;
		String result = "";
		result += nrOfStates + delimiter;
		result += nrOfBlueVisists + delimiter;
		result += nrOfRedVisists + delimiter;
		result += nrOfUnvisitedBlues + delimiter;
		result += nrOfUnvisitedReds + delimiter;
		result += (nrOfBlueVisists / (nrOfStates - nrOfUnvisitedBlues)) + delimiter;
		result += (nrOfRedVisists / (nrOfStates - nrOfUnvisitedReds)) + delimiter; 
		result += aveNrOfBlueNodes + delimiter; 
		result += aveNrOfRedNodes + delimiter;
		result += blueNodeStdDev + delimiter;
		result += redNodeStdDev;
		return result;
	}

	public String getResultsUser() {
		String result = "";
		result += ("Total amount of states: " + nrOfStates + "\n");
		result += ("Total number of blue visits: " + nrOfBlueVisists + "\n");
		result += ("Total number of red visits: " + nrOfRedVisists + "\n");
		result += ("Total number of unvisited blue states: " + nrOfUnvisitedBlues + "\n");
		result += ("Total number of unvisited red states: " + nrOfUnvisitedReds + "\n");
		result += ("Blue overlap coefficient: " + (nrOfBlueVisists / (nrOfStates - nrOfUnvisitedBlues)) + "\n");
		result += ("Red overlap coefficient: " + (nrOfRedVisists / (nrOfStates - nrOfUnvisitedReds)) + "\n");
		result += ("Blue visit average: " + aveNrOfBlueNodes + "\n");
		result += ("Red visit average: " + aveNrOfRedNodes + "\n");
		result += ("Blue visit std dev: " + blueNodeStdDev + "\n");
		result += ("Red visit std dev: " + redNodeStdDev + "\n");
		return result;
	}

	public void printHeartBeats() {
    	long totalTime = end - start;
    	String delimiter = Global.CSV_DELIMITER;
    	
    	System.out.println();
    	System.out.println(
    			"time" + delimiter +
    			"dfsBlue start counter" + delimiter + 
    			"dfsBlue done counter" + delimiter + 
    			"dfsBlue counter" + delimiter + 
    			"dfsRed start counter" + delimiter + 
    			"dfsRed done counter" + delimiter + 
    			"dfsRed counter" + delimiter + 
    			"wait counter");
    	
		for (long i = 0; i < totalTime; i++) {
			if (dfsBlueStartCount.get(i).get() == -1) {
				continue;
			}
			System.out.println(
					i + delimiter + // milliseconds
					dfsBlueStartCount.get(i).get() + delimiter + // dfsBlue start counter
					dfsBlueDoneCount.get(i).get() + delimiter + // dfsBlue done counter
					dfsBlueCount.get(i).get() + delimiter + // dfsBlue counter
					dfsRedStartCount.get(i).get() + delimiter + // dfsRed start counter
					dfsRedDoneCount.get(i).get() + delimiter + // dfsRed done counter
					dfsRedCount.get(i).get() + delimiter + // dfsRed counter
					waitCount.get(i).get()// wait counter
				);
		}
	}

}
