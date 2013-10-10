package mcndfs.opt4;

import graph.State;
import helperClasses.BooleanHashMap;
import helperClasses.Color;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.CycleFound;
import ndfs.Result;

public class NNDFS extends MCNDFS {

	int nrOfThreads;

    class Bird extends GeneralBird {
    	
    	private int threads;
    	private int depth;
    	private int counter;
    	private Map<State, Boolean> localStateRed;
    	
        Bird(int id) {
        	super(id, file);
        	
        	this.threads = nrOfThreads;
        	this.depth = 0;
        	this.counter = 0;
        	this.localStateRed = new BooleanHashMap<State>(new Boolean(false));
        }

        @Override
        protected void dfsRed(State s) throws Result, InterruptedException {
        	super.dfsRed(s);
        	
            boolean tRed;
            List<State> post;

            localStatePink.put(s, true);

            post = graph.post(s);
            Collections.shuffle(post, rand);

            for (State t : post) {
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
	                stateCount.notifyAll();
                }
            }

            synchronized (stateRed) {
                stateRed.put(s, true);
            }
            localStateRed.put(s, true);
            localStatePink.put(s, false);
            
        }

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
        	super.dfsBlue(s);
        	
        	depth++;
        	
        	//System.out.println("IN:: Id: " + id + " NrThr: " + threads + " Depth: " + depth + " Count: " + counter);
        	
            boolean tRed;
            boolean allRed = true;
            List<State> post;

            localColors.color(s, Color.CYAN);

            post = graph.post(s);
            Collections.shuffle(post, rand);

            for (State t : post) {
            	// early cycle detection
            	if ( localColors.hasColor(t, Color.CYAN) && s.isAccepting() && t.isAccepting() ) {
            		throw new CycleFound(id);
            	}
            	
            	if (depth <= threads) {
            		synchronized (stateRed) {
	                    tRed = stateRed.get(t);
	                }
            	} else if (counter < (threads + (depth/threads))/3) {
            		tRed = localStateRed.get(t);
            		counter++;
            	} else {
            		synchronized (stateRed) {
	                    tRed = stateRed.get(t);
	                }
            		counter = 0;
            	}
            	
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
                
                // allred
                if (depth <= threads/2) {
	                synchronized (stateRed) {
	                    tRed = stateRed.get(t);
	                }
                } else if (counter < (threads + (depth/threads))/3) {
                	tRed = localStateRed.get(t);
                	counter++;
            	} else {
            		synchronized (stateRed) {
	                    tRed = stateRed.get(t);
	                }
            		counter = 0;
            	}
                if (! tRed) {
                	allRed = false;
                }
            }

            if (allRed) {
                synchronized (stateRed) {
                    stateRed.put(s, true);
                }
                localStateRed.put(s, true);
            } else if (s.isAccepting()) {
                synchronized (stateCount) {
                    int count = stateCount.get(s);
                    stateCount.put(s, count + 1);
                }

                dfsRed(s);
            }

            localColors.color(s, Color.BLUE);
            
            depth--;
            
        	//System.out.println("OUT:: Id: " + id + " NrThr: " + threads + " Depth: " + depth + " Count: " + counter);
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
    }

    @Override
    public void init(int nrOfThreads) {
    	this.nrOfThreads = nrOfThreads;
    	for (int i = 1; i <= nrOfThreads; i++) {
    		super.swarm.add(new Bird(i));
    	}
    }
    
}
