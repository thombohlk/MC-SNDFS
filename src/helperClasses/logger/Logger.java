package helperClasses.logger;

import graph.Graph;
import graph.GraphFactory;
import graph.State;

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

import ndfs.AlgorithmResult;

public class Logger {
	
	private long start, end;
	private Timer timer;

    private ConcurrentLongHashMap<Long> dfsBlueCount;
    private ConcurrentLongHashMap<Long> dfsBlueStartCount;
    private ConcurrentLongHashMap<Long> dfsBlueDoneCount;
    private ConcurrentLongHashMap<Long> dfsRedCount;
    private ConcurrentLongHashMap<Long> dfsRedStartCount;
    private ConcurrentLongHashMap<Long> dfsRedDoneCount;
    private ConcurrentLongHashMap<Long> waitCount;

    private ConcurrentHashMap<Integer, HashSet<State>> stateBlueVisits;
    private ConcurrentHashMap<Integer, HashSet<State>> stateRedVisits;

    private AtomicLong dfsBlueCounter;
    private AtomicLong dfsBlueStartCounter;
    private AtomicLong dfsBlueDoneCounter;
    private AtomicLong dfsRedCounter;
    private AtomicLong dfsRedStartCounter;
    private AtomicLong dfsRedDoneCounter;
    private AtomicLong waitCounter;
    
	private Graph graph;
	private GraphAnalyser graphAnalyser;

    public Logger(Graph graph) {
    	initVariables();
    	this.graph = graph;
    }


    public Logger(File file) {
    	initVariables();
    	try {
			this.graph = GraphFactory.createGraph(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    // TODO: remove this??
    public Logger() {
    	initVariables();
    }
    
    private void initVariables() {
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

    	stateBlueVisits = new ConcurrentHashMap<Integer, HashSet<State>>();
    	stateRedVisits = new ConcurrentHashMap<Integer, HashSet<State>>();
	}

	public void start() {
    	start = System.currentTimeMillis();
		timer = new Timer();
//		timer.schedule(new TimerTask() {
//			public void run()  {
//				long time = System.currentTimeMillis() - start;
//				logIteration(time);
//			}
//		}, 1, 1);
    }
    
    public void stop() {
    	end = System.currentTimeMillis();
    	timer.cancel();
    }

	public void logIteration(long time) {
		dfsBlueCount.put(time, dfsBlueCounter);
		dfsBlueStartCount.put(time, dfsBlueStartCounter);
		dfsBlueDoneCount.put(time, dfsBlueDoneCounter);
		dfsRedCount.put(time, dfsRedCounter);
		dfsRedStartCount.put(time, dfsRedStartCounter);
		dfsRedDoneCount.put(time, dfsRedDoneCounter);
		waitCount.put(time, waitCounter);
	}

    public void parseData() {
    	analyseOverlap();
//    	printAverageNrOfNodes("blue", stateBlueVisits);
//    	printAverageNrOfNodes("red", stateRedVisits);
//    	printHeartBeats();
	}

	private void analyseOverlap() {
		graphAnalyser = new GraphAnalyser(this.graph, this.stateBlueVisits, this.stateRedVisits);
		graphAnalyser.count();
//		graphAnalyser.printResults();
	}

	private void printHeartBeats() {
    	long totalTime = end - start;
		for (long i = 0; i < totalTime; i++) {
			if (dfsBlueStartCount.get(i).get() == -1) {
				continue;
			}
			System.out.println(
					i + ", " + // milliseconds
					dfsBlueStartCount.get(i).get() + ", " + // dfsBlue start counter
					dfsBlueDoneCount.get(i).get() + ", " + // dfsBlue done counter
					dfsBlueCount.get(i).get() + ", " + // dfsBlue counter
					" ," + // empty column
					dfsRedStartCount.get(i).get() + ", " + // dfsRed start counter
					dfsRedDoneCount.get(i).get() + ", " + // dfsRed done counter
					dfsRedCount.get(i).get() + ", " + // dfsRed counter
					" ," + // empty column
					waitCount.get(i).get()// wait counter
				);
		}
	}

	private void printAverageNrOfNodes(String color,
			ConcurrentHashMap<Integer, HashSet<State>> visitedStates) {
		
		double total = 0;
		int nrOfThreads = 0;
		for (Integer i : visitedStates.keySet()) {
			total += visitedStates.get(i).size();
			System.out.println(i + ": " + visitedStates.get(i).size());
			nrOfThreads++;
		}
		double average = total / nrOfThreads;

		System.out.println(color + ": " + average);
		
	}

	synchronized public void logDfsBlueStart(int id, State s) {
		dfsBlueCounter.incrementAndGet();
		dfsBlueStartCounter.incrementAndGet();

		HashSet<State> set;
		if (stateBlueVisits.containsKey(id)) {
			set = stateBlueVisits.get(id);
		} else {
			set = new HashSet<State>();
		}
		set.add(s);
		stateBlueVisits.put(id, set);
	}

	synchronized public void logDfsBlueDone() {
		dfsBlueCounter.decrementAndGet();
		dfsBlueDoneCounter.incrementAndGet();
	}

	synchronized public void logDfsRedStart(int id, State s) {
		dfsRedCounter.incrementAndGet();
		dfsRedStartCounter.incrementAndGet();
		
		HashSet<State> set;
		if (stateRedVisits.containsKey(id)) {
			set = stateRedVisits.get(id);
		} else {
			set= new HashSet<State>();
		}
		set.add(s);
		stateRedVisits.put(id, set);
	}

	synchronized public void logDfsRedDone() {
		dfsRedCounter.decrementAndGet();
		dfsRedDoneCounter.incrementAndGet();
	}

	synchronized public void logIncrementWait() {
    	waitCounter.incrementAndGet();
	}

	synchronized public void logDecrementWait() {
    	waitCounter.decrementAndGet();
	}

	public String getResultsCSV() {
		String result = "";
		result += graphAnalyser.getResultsCSV();
		
		return result;
	}


	public GraphAnalyser getGraphAnalyser() {
		return this.graphAnalyser;
	}


	public void setGraphAnalyser(GraphAnalyser graphAnalyser) {
		this.graphAnalyser = graphAnalyser;
	}


	public static Logger calculateAverageLogger(AlgorithmResult[] results) {
		Logger logger = new Logger();
		GraphAnalyser ga;

		int totalNrOfUnvisitedBlueStates = 0;
		int totalNrOfUnvisitedRedStates = 0;
		int totalNrOfBlueVisists = 0;
		int totalNrOfRedVisists = 0;
		
		for (int i = 0; i < results.length; i++) {
			ga = results[i].getLogger().getGraphAnalyser();
			totalNrOfUnvisitedBlueStates += ga.totalNrOfUnvisitedBlueStates;
			totalNrOfUnvisitedRedStates += ga.totalNrOfUnvisitedRedStates;
			totalNrOfBlueVisists += ga.totalNrOfBlueVisists;
			totalNrOfRedVisists += ga.totalNrOfRedVisists;
		}
		
		ga = new GraphAnalyser(null, null, null);
		ga.totalNrOfStates = (int) results[0].getLogger().getGraphAnalyser().totalNrOfStates;
		ga.totalNrOfUnvisitedBlueStates = totalNrOfUnvisitedBlueStates / results.length;
		ga.totalNrOfUnvisitedRedStates = totalNrOfUnvisitedRedStates / results.length;
		ga.totalNrOfBlueVisists = totalNrOfBlueVisists / results.length;
		ga.totalNrOfRedVisists = totalNrOfRedVisists / results.length;
		
		logger.setGraphAnalyser(ga);
		
		return logger;
	}


	public String getResultsUser() {
		String result = graphAnalyser.getResultsUser();
		return result;
	}
}
