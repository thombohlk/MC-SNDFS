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
import java.util.concurrent.atomic.AtomicLong;

import driver.Analyser;

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
//		graphAnalyser.analyseOverlap();
    	graphAnalyser.analyseAverage();
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


	public void logWaitingTime(long id, long waitingTime) {
		data.waitingTime.put(id, new AtomicLong(waitingTime));
	}
}
