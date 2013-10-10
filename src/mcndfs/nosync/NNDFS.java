package mcndfs.nosync;

import graph.State;
import helperClasses.Color;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.CycleFound;
import ndfs.Result;

public class NNDFS extends MCNDFS {

	protected ConcurrentBooleanHashMap<State> stateRed;
	protected ConcurrentIntegerHashMap<State> stateCount;

    class Bird extends GeneralBird {

        Bird(int id) {
        	super(id, file);
        	stateRed = new ConcurrentBooleanHashMap<>(new AtomicBoolean(false));
        	stateCount = new ConcurrentIntegerHashMap<>(new AtomicInteger(0));
        }

        @Override
        protected void dfsRed(State s) throws Result, InterruptedException {
        	super.dfsRed(s);
        	
            List<State> post;

            localStatePink.put(s, true);

            post = graph.post(s);
            Collections.shuffle(post, this.rand);

            for (State t : post) {
                if (localColors.hasColor(t, Color.CYAN)) {
                    throw new CycleFound(id);
                }

                if (! localStatePink.get(t).booleanValue() && ! stateRed.get(t).get()) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                stateCount.get(s).decrementAndGet();

                start = System.currentTimeMillis();
                synchronized(stateCount) {
                	waitingTime += System.currentTimeMillis() - start;
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notifyAll();
                }
            }

            stateRed.put(s, new AtomicBoolean(true));
            localStatePink.put(s, false);
        }

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
        	super.dfsBlue(s);
        	
            boolean allRed = true;
            List<State> post;

            localColors.color(s, Color.CYAN);

            post = graph.post(s);
            Collections.shuffle(post, this.rand);

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
    	for (int i = 1; i <= nrOfThreads; i++) {
    		this.swarm.add(new Bird(i));
    	}
    }

}
