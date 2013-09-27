package ndfs.mcndfs_nosync;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import graph.GraphFactory;
import graph.State;
import graph.Graph;
import helperClasses.BooleanHashMap;
import helperClasses.Colors;
import helperClasses.Color;
import helperClasses.IntegerHashMap;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;

public class NNDFS implements NDFS {

    volatile private ConcurrentBooleanHashMap<State> stateRed;
    volatile private ConcurrentIntegerHashMap<State> stateCount;

    private ArrayList<Bird> swarm;
    private File file;


    class Bird implements Callable<Integer> {

        int id;
        private Graph graph;
        private State initialState;
        private Colors localColors;
        private Map<State, Boolean> localStatePink;


        Bird(int id) {
            try {
                this.graph = GraphFactory.createGraph(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            this.id = id;
            this.initialState = graph.getInitialState();
            this.localStatePink = new BooleanHashMap<State>(new Boolean(false));
            this.localColors = new Colors(new HashMap<State, Color>());
        }

        /**
         * Start algorithm.
         * 
         * @return Integer Returns -id if cycle has been found, otherwise id.
         */
        public Integer call() throws Exception {
            try {
                dfsBlue(initialState);
            } catch (Result e) {
                return -(this.id);
            }

            return this.id;
        }


        private void dfsRed(State s) throws Result, InterruptedException {
            AtomicBoolean tRed;
            List<State> post;

            localStatePink.put(s, true);

            post = graph.post(s);
            Collections.shuffle(post);

            for (State t : post) {
                if (localColors.hasColor(t, Color.CYAN)) {
                    throw new CycleFound();
                }

                if (! localStatePink.get(t).booleanValue() && ! stateRed.get(t).get()) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                stateCount.get(s).decrementAndGet();

                synchronized(stateCount) {
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notify();
                }
            }

            stateRed.put(s, new AtomicBoolean(true));
            localStatePink.put(s, false);
        }


        private void dfsBlue(State s) throws Result, InterruptedException {
            boolean tRed;
            boolean allRed = true;
            List<State> post;

            localColors.color(s, Color.CYAN);

            post = graph.post(s);
            Collections.shuffle(post);

            for (State t : post) {
            	// early cycle detection
            	if ( localColors.hasColor(t, Color.CYAN) && s.isAccepting() && t.isAccepting() ) {
            		throw new CycleFound(id);
            	}

            	if (localColors.hasColor(t, Color.WHITE) && ! stateRed.get(t).get()) {
                    dfsBlue(t);
                }
                
                // allred
                if (! stateRed.get(t).get()) {
                	allRed = false;
                }
            }

            if (allRed) {
                stateRed.put(s, new AtomicBoolean(true));
            } else if (s.isAccepting()) {
                stateCount.get(s).incrementAndGet();

                dfsRed(s);
            }

            localColors.color(s, Color.BLUE);
        }

    }


    public NNDFS(File file) {
        this.file = file;
        this.stateRed = new ConcurrentBooleanHashMap<State>(new AtomicBoolean(false));
        this.stateCount = new ConcurrentIntegerHashMap<State>(new AtomicInteger(0));
    }


    public void init(int nrOfThreads) {
    	this.swarm = new ArrayList<Bird>();
    	for (int i = 1; i <= nrOfThreads; i++) {
    		this.swarm.add(new Bird(i));
    	}
    }

    private void nndfs() throws Result {
        boolean foundCycle = false;
        int foundBy = 0;
        
        ExecutorService ex = Executors.newFixedThreadPool(swarm.size());
        CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(ex);
        
        // setup threads for each of the callables 
        for (int i = 0; i < this.swarm.size(); i++) {
            cs.submit(swarm.get(i));
        }

        // Wait for the first thread to return. If an exception is thrown the 
        // completion service is shut down and a CycleFound will be thrown.
        try {
			int result = cs.take().get();
			if (result > 0) {
				foundBy = result;
			} else {
				foundBy = -result;
				foundCycle = true;
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ex.shutdownNow();

        if (foundCycle) {
            throw new CycleFound(foundBy);
        } else {
            throw new NoCycleFound();
        }
    }


    public void ndfs() throws Result {
        nndfs();
    }

}
