package ndfs.mcndfs_optimalPermutation;

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

    volatile private BooleanHashMap<State> stateRed;
    volatile private Map<State, Integer> stateCount;

    private ArrayList<Bird> swarm;
    private File file;
    private int nrOfThreads;


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


        public Integer call() throws Exception {
            try {
                dfsBlue(initialState);
            } catch (Result e) {
                throw new Exception(e);
            }

            return this.id;
        }


        private void dfsRed(State s) throws Result, InterruptedException {
            boolean tRed;

            localStatePink.put(s, true);

            for (State t : getPermutation(s)) {
                if (localColors.hasColor(t, Color.CYAN)) {
                    throw new CycleFound();
                }

                synchronized (stateRed) {
                    tRed = stateRed.get(t);
                }
                if (! localStatePink.get(t).booleanValue() && ! tRed) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                synchronized(stateCount) {
                    int count = stateCount.get(s).intValue();
                    stateCount.put(s, count - 1);
                }

                synchronized(stateCount) {
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notify();
                }
            }

            synchronized (stateRed) {
                stateRed.put(s, true);
            }
            localStatePink.put(s, false);
        }

        private void dfsBlue(State s) throws Result, InterruptedException {
            boolean tRed;
            boolean allRed = true;

            localColors.color(s, Color.CYAN);

            for (State t : getPermutation(s)) {
            	// early cycle detection
            	if ( localColors.hasColor(t, Color.CYAN) && s.isAccepting() && t.isAccepting() ) {
            		throw new CycleFound();
            	}
            	
                synchronized (stateRed) {
                    tRed = stateRed.get(t);
                }
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
                
                // allred
                synchronized (stateRed) {
                    if (! stateRed.get(t)) {
                    	allRed = false;
                    }
                }
            }

            if (allRed) {
                synchronized (stateRed) {
                    stateRed.put(s, true);
                }
            } else if (s.isAccepting()) {
                synchronized (stateCount) {
                    int count = stateCount.get(s);
                    stateCount.put(s, count + 1);
                }

                dfsRed(s);
            }

            localColors.color(s, Color.BLUE);
        }
        
        // TODO: get this fucker to work properly!!
        private List<State> getPermutation(State s) {
            List<State> post = graph.post(s);
            int size = post.size();
            
            if (size > 0) {
            	int dummyId = (int) (id / 2.) + 1;
                int a = post.size() / nrOfThreads;
                int b = a * dummyId;
                int c = b % post.size();
                Collections.rotate(post, c);
                if (id % 2 == 1) {Collections.reverse(post);}
            }
            
            return post;
        }

    }


    public NNDFS(File file) {
        this.file = file;
        this.stateRed = new BooleanHashMap<State>(new Boolean(false));
        this.stateCount = new IntegerHashMap<State>(new Integer(0));
    }


    public void init(int nrOfThreads) {
    	this.nrOfThreads = nrOfThreads;
    	this.swarm = new ArrayList<Bird>();
    	for (int i = 0; i < nrOfThreads; i++) {
    		this.swarm.add(new Bird(i));
    	}
    }

    private void nndfs() throws Result {
        ExecutorService ex = Executors.newFixedThreadPool(swarm.size());
        CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(ex);

        boolean foundCycle = false;
        
        // setup threads for each of the callables 
        for (int i = 0; i < this.swarm.size(); i++) {
            cs.submit(swarm.get(i));
        }

        // Wait for the first thread to return. If an exception is thrown the 
        // completion service is shut down and a CycleFound will be thrown.
        for (int i = 0; i < this.swarm.size(); i++) {
            try {
                cs.take().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                foundCycle = true;
                break;
            }
        }
        ex.shutdownNow();

        if (foundCycle) {
            throw new CycleFound();
        } else {
            throw new NoCycleFound();
        }
    }


    public void ndfs() throws Result {
        nndfs();
    }

}
