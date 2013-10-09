package helperClasses.logger;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
import helperClasses.ConcurrentLongHashMap;
import helperClasses.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import driver.Analyser;
import ndfs.AlgorithmResult;

public class AlgorithmLogger {
	
	private Timer timer;
    
	private GraphAnalyser graphAnalyser;
	private GraphAnalysisDataObject data;

    public AlgorithmLogger(Graph graph) {
    	this.data = new GraphAnalysisDataObject(graph);
		this.graphAnalyser = new GraphAnalyser(this.data);
    }


    public AlgorithmLogger(File file) {
    	try {
			Graph graph = GraphFactory.createGraph(file);
	    	this.data = new GraphAnalysisDataObject(graph);
			this.graphAnalyser = new GraphAnalyser(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    
	public void start() {
    	data.start = System.currentTimeMillis();
		timer = new Timer();
		
		if (Global.MODE.equals(Analyser.MODE_HEARTBEAT)) {
			timer.schedule(new TimerTask() {
				public void run()  {
					long time = System.currentTimeMillis() - data.start;
					logIteration(time);
				}
			}, 1, 1);
		}
    }
    
    public void stop() {
    	data.end = System.currentTimeMillis();
    	timer.cancel();
    }

	public void logIteration(long time) {
		data.dfsBlueCount.put(time, data.dfsBlueCounter);
		data.dfsBlueStartCount.put(time, data.dfsBlueStartCounter);
		data.dfsBlueDoneCount.put(time, data.dfsBlueDoneCounter);
		data.dfsRedCount.put(time, data.dfsRedCounter);
		data.dfsRedStartCount.put(time, data.dfsRedStartCounter);
		data.dfsRedDoneCount.put(time, data.dfsRedDoneCounter);
		data.waitCount.put(time, data.waitCounter);
	}

    public void parseData() {
		graphAnalyser.count();
//    	printAverageNrOfNodes("blue", stateBlueVisits);
//    	printAverageNrOfNodes("red", stateRedVisits);
//    	printHeartBeats();
	}

	private void printAverageNrOfNodes(String color,
			ConcurrentHashMap<Integer, HashSet<State>> visitedStates) {
		
		double total = 0;
		double average = 0;
		int nrOfThreads = 0;
		
		for (Integer i : visitedStates.keySet()) {
			total += visitedStates.get(i).size();
			System.out.println(i + ": " + visitedStates.get(i).size());
			nrOfThreads++;
		}
		average = total / nrOfThreads;

		System.out.println(color + ": " + average);
		
	}

	synchronized public void logDfsBlueStart(int id, State s) {
		data.dfsBlueCounter.incrementAndGet();
		data.dfsBlueStartCounter.incrementAndGet();

		HashSet<State> set;
		if (data.stateBlueVisits.containsKey(id)) {
			set = data.stateBlueVisits.get(id);
		} else {
			set = new HashSet<State>();
		}
		set.add(s);
		data.stateBlueVisits.put(id, set);
	}

	synchronized public void logDfsBlueDone() {
		data.dfsBlueCounter.decrementAndGet();
		data.dfsBlueDoneCounter.incrementAndGet();
	}

	synchronized public void logDfsRedStart(int id, State s) {
		data.dfsRedCounter.incrementAndGet();
		data.dfsRedStartCounter.incrementAndGet();
		
		HashSet<State> set;
		if (data.stateRedVisits.containsKey(id)) {
			set = data.stateRedVisits.get(id);
		} else {
			set= new HashSet<State>();
		}
		set.add(s);
		data.stateRedVisits.put(id, set);
	}

	synchronized public void logDfsRedDone() {
		data.dfsRedCounter.decrementAndGet();
		data.dfsRedDoneCounter.incrementAndGet();
	}

	synchronized public void logIncrementWait() {
		data.waitCounter.incrementAndGet();
	}

	synchronized public void logDecrementWait() {
		data.waitCounter.decrementAndGet();
	}


	public GraphAnalyser getGraphAnalyser() {
		return this.graphAnalyser;
	}


	public void setGraphAnalyser(GraphAnalyser graphAnalyser) {
		this.graphAnalyser = graphAnalyser;
	}


	public String getResultsUser() {
		return getAnalysisData().getResultsUser();
	}

	
	public String getResultsCSV() {
		return getAnalysisData().getResultsCSV();
	}


	public GraphAnalysisDataObject getAnalysisData() {
		return graphAnalyser.getData();
	}
}
