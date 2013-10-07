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
import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;

public class NNDFS extends MCNDFS {

    volatile private Map<State, Integer> stateVisited;

    class Bird extends GeneralBird {

        Bird(int id) {
        	super(id, file);
        }
        
        @Override
        protected void dfsRed(State s) throws Result, InterruptedException {
        	super.dfsRed(s);
        	
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

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
        	super.dfsBlue(s);
        	
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
        
        @Override
        protected void terminate() throws Result {
        	synchronized(stateCount) {
                stateCount.notifyAll();
            }
        	super.terminate();
        }

    }


    public NNDFS(File file) {
    	super(file);
    	this.stateVisited = new IntegerHashMap<State>(new Integer(0));
    }

    @Override
    public void init(int nrOfThreads) {
    	for (int i = 1; i <= nrOfThreads; i++) {
    		super.swarm.add(new Bird(i));
    	}
    }

}
