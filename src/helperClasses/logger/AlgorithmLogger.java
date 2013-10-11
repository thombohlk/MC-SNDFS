package helperClasses.logger;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
import helperClasses.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import driver.Analyser;

/**
 * Class that will certain actions made by the algorithms.
 * @author thomas
 *
 */
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
    
    /**
     * Starts the timer to keep track of data in heartbeat mode.
     */
	public void start() {
		timer = new Timer();
    	data.start = System.currentTimeMillis();
		
		if (Global.MODE.equals(Analyser.MODE_HEARTBEAT)) {
			timer.schedule(new TimerTask() {
				public void run()  {
					long time = System.currentTimeMillis() - data.start;
					logIteration(time);
				}
			}, 1, 1);
		}
    }

	/**
	 * Makes sure the timer is stopped.
	 */
    public void stop() {
    	data.end = System.currentTimeMillis();
    	timer.cancel();
    }

    /**
     * Logs the current values of the logging variables for timestep time.
     * @param time
     */
	public void logIteration(long time) {
		data.dfsBlueCount.put(time, data.dfsBlueCounter);
		data.dfsBlueStartCount.put(time, data.dfsBlueStartCounter);
		data.dfsBlueDoneCount.put(time, data.dfsBlueDoneCounter);
		data.dfsRedCount.put(time, data.dfsRedCounter);
		data.dfsRedStartCount.put(time, data.dfsRedStartCounter);
		data.dfsRedDoneCount.put(time, data.dfsRedDoneCounter);
		data.waitCount.put(time, data.waitCounter);
	}

	/**
	 * Analyses the data using the graph analyser class.
	 */
    public void parseData() {
		graphAnalyser.analyseOverlap();
    	graphAnalyser.analyseAverage();
	}

    /**
     * Logs the start of a dfs blue call.
     * @param id
     * @param s
     */
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
    /**
     * Logs the end of a dfs blue call.
     * @param id
     * @param s
     */
	synchronized public void logDfsBlueDone() {
		data.dfsBlueCounter.decrementAndGet();
		data.dfsBlueDoneCounter.incrementAndGet();
	}
	
    /**
     * Logs the start of a dfs red call.
     * @param id
     * @param s
     */
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
	
    /**
     * Logs the end of a dfs red call.
     * @param id
     * @param s
     */
	synchronized public void logDfsRedDone() {
		data.dfsRedCounter.decrementAndGet();
		data.dfsRedDoneCounter.incrementAndGet();
	}

	/**
	 * Increments the wait counter.
	 */
	synchronized public void logIncrementWait() {
		data.waitCounter.incrementAndGet();
	}

	/**
	 * Decrements the wait counter.
	 */
	synchronized public void logDecrementWait() {
		data.waitCounter.decrementAndGet();
	}

	/**
	 * Returns the graph analyser.
	 * @return
	 */
	public GraphAnalyser getGraphAnalyser() {
		return this.graphAnalyser;
	}

	/**
	 * Sets a graph analyser.
	 * @param graphAnalyser
	 */
	public void setGraphAnalyser(GraphAnalyser graphAnalyser) {
		this.graphAnalyser = graphAnalyser;
	}

	/**
	 * Returns a user friendly output of the data.
	 * @return
	 */
	public String getResultsUser() {
		return getAnalysisData().getResultsUser();
	}

	/**
	 * Returns CSV output of the logged data.
	 * @return
	 */
	public String getResultsCSV() {
		return getAnalysisData().getResultsCSV();
	}

	/**
	 * Returns the analysis data.
	 * @return
	 */
	public GraphAnalysisDataObject getAnalysisData() {
		return graphAnalyser.getData();
	}
}
