package ndfs.mcndfs_optimalPermutation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
    volatile private Map<State, Integer> stateVisited;

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
                return -(this.id);
            }

            return this.id;
        }


        private void dfsRed(State s) throws Result, InterruptedException {
        	synchronized (stateVisited) {
        		int value = stateVisited.get(s);
				stateVisited.put(s, value + 1);
			}
        	
            boolean tRed;

            localStatePink.put(s, true);

            List<State> post = graph.post(s);
            while (! post.isEmpty()) {
            	if (s.equals(graph.getInitialState()) && id == 0) {
            		System.out.println("red: " + post.size());
            	}
            	State t = getLeastVisited(post);
            	
                if (localColors.hasColor(t, Color.CYAN)) {
            		System.out.println("better late than never");
                    throw new CycleFound();
                }

                synchronized (stateRed) {
                    tRed = stateRed.get(t);
                }
                if (! localStatePink.get(t).booleanValue() && ! tRed) {
                    dfsRed(t);
                }
            	post.remove(t);
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
	                stateCount.notifyAll();
                }
            }

            synchronized (stateRed) {
                stateRed.put(s, true);
            }
            localStatePink.put(s, false);
        }

        private void dfsBlue(State s) throws Result, InterruptedException {
        	synchronized (stateVisited) {
        		int value = stateVisited.get(s);
				stateVisited.put(s, value + 1);
			}
        	
            boolean tRed;
            boolean allRed = true;

            localColors.color(s, Color.CYAN);

            List<State> post = graph.post(s);
            while (! post.isEmpty()) {
            	State t = getLeastVisited(post);
            	
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
            	post.remove(t);
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
        
        private State getLeastVisited(List<State> post) {
        	int leastVisitedNr = Integer.MAX_VALUE;
        	int visited;
        	State leastVisited = post.get(0);
        	
        	for (State t : post) {
        		synchronized (stateVisited) {
        			visited = stateVisited.get(t);
				}
        		if (visited < leastVisitedNr) {
        			leastVisited = t;
        			leastVisitedNr = visited;
        		}
        	}
            
            return leastVisited;
        }

    }


    public NNDFS(File file) {
        this.file = file;
        this.stateRed = new BooleanHashMap<State>(new Boolean(false));
        this.stateCount = new IntegerHashMap<State>(new Integer(0));
        this.stateVisited = new IntegerHashMap<State>(new Integer(0));
    }


    public void init(int nrOfThreads) {
    	this.nrOfThreads = nrOfThreads;
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


	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
		
	}

}
