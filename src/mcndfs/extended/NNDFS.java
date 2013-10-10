package mcndfs.extended;

import graph.State;
import helperClasses.Color;

import java.io.File;
import java.util.Collections;
import java.util.List;

import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.CycleFound;
import ndfs.Result;

public class NNDFS extends MCNDFS {


    class Bird extends GeneralBird {

        Bird(int id) {
        	super(id, file);
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

                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    tRed = stateRed.get(t);
                }
                if (! localStatePink.get(t).booleanValue() && ! tRed) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                start = System.currentTimeMillis();
                synchronized(stateCount) {
                	waitingTime += System.currentTimeMillis() - start;
                    int count = stateCount.get(s).intValue();
                    stateCount.put(s, count - 1);
                }

                start = System.currentTimeMillis();
                synchronized(stateCount) {
                	waitingTime += System.currentTimeMillis() - start;
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notifyAll();
                }
            }

            start = System.currentTimeMillis();
            synchronized (stateRed) {
            	waitingTime += System.currentTimeMillis() - start;
                stateRed.put(s, true);
            }
            localStatePink.put(s, false);
        }

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
        	super.dfsBlue(s);

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

                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    tRed = stateRed.get(t);
                }
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
                
                // allred
                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    tRed = stateRed.get(t);
                }
                if (! tRed) {
                	allRed = false;
                }
            }

            if (allRed) {
                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    stateRed.put(s, true);
                }
            } else if (s.isAccepting()) {
                start = System.currentTimeMillis();
                synchronized (stateCount) {
                	waitingTime += System.currentTimeMillis() - start;
                    int count = stateCount.get(s);
                    stateCount.put(s, count + 1);
                }

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
    		super.swarm.add(new Bird(i));
    	}
    }
    
}
