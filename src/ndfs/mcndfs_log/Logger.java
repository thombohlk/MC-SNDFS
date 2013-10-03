package ndfs.mcndfs_log;

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
    
    private File file;
    	
	class GraphCounter {

		private Graph graph;
		
		private double totalNrOfStates = 0;
		private int totalNrOfUnvisitedBlueStates = 0;
		private int totalNrOfUnvisitedRedStates = 0;
		private int totalNrOfBlueVisists = 0;
		private int totalNrOfRedVisists = 0;
		
		private HashSet<State> visitedStates;
		
		public GraphCounter() {
			visitedStates = new HashSet<State>();
	        try {
				this.graph = GraphFactory.createGraph(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
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
			System.out.println();
			System.out.println("Total amount of states: " + totalNrOfStates);
			System.out.println("Total number of blue visits: " + totalNrOfBlueVisists);
			System.out.println("Total number of red visits: " + totalNrOfRedVisists);
			System.out.println("Total number of unvisited blue states: " + totalNrOfUnvisitedBlueStates);
			System.out.println("Total number of unvisited red states: " + totalNrOfUnvisitedRedStates);
			System.out.println("Blue overlap coefficient: " + (totalNrOfBlueVisists / (totalNrOfStates - totalNrOfUnvisitedBlueStates)) );
			System.out.println("Red overlap coefficient: " + (totalNrOfRedVisists / (totalNrOfStates - totalNrOfUnvisitedRedStates)) );
			System.out.println();
		}
		
	}

    public Logger(File file) {
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
    	
    	this.file = file;
    }
    
    public void start() {
    	start = System.currentTimeMillis();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run()  {
				long time = System.currentTimeMillis() - start;
				logIteration(time);
			}
		}, 1, 1);
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

    public void printLogs() {
    	printAverageNrOfNodes("blue", stateBlueVisits);
    	printAverageNrOfNodes("red", stateRedVisits);
    	printOverlap();
//    	printHeartBeats();
	}

	private void printOverlap() {
		GraphCounter gc = new GraphCounter();
		gc.count();
		gc.printResults();
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
}
