package mcndfs.lock;

import graph.State;
import helperClasses.Color;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.CycleFound;
import ndfs.Result;

public class NNDFS extends MCNDFS {

    private final ReentrantLock redLock = new ReentrantLock();
    private final ReentrantLock countLock = new ReentrantLock();
    private final Condition countZero = countLock.newCondition();

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
            Collections.shuffle(post, this.rand);

            for (State t : post) {
                if (localColors.hasColor(t, Color.CYAN)) {
                    throw new CycleFound();
                }

                redLock.lock();
                try {
                	tRed = stateRed.get(t);
                } finally {
                	redLock.unlock();
                }
                
                if (! localStatePink.get(t).booleanValue() && ! tRed) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                countLock.lock();
                try {
	                int count = stateCount.get(s).intValue();
	                stateCount.put(s, count - 1);

	                while (stateCount.get(s).intValue() > 0) {
	                	countZero.await();
	                }
	                countZero.signalAll();
                } finally {
                	countLock.unlock();
                }
            }

            redLock.lock();
            try {
            	stateRed.put(s, true);
            } finally {
            	redLock.unlock();
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
            Collections.shuffle(post, this.rand);

            for (State t : post) {
            	// early cycle detection
            	if ( localColors.hasColor(t, Color.CYAN) && s.isAccepting() && t.isAccepting() ) {
            		throw new CycleFound();
            	}

                redLock.lock();
                try {
                	tRed = stateRed.get(t);
                } finally {
                	redLock.unlock();
                }
                
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
                
                // allred
                redLock.lock();
                if (! stateRed.get(t)) {
                	allRed = false;
                }
                redLock.unlock();
            }

            if (allRed) {
                redLock.lock();
                try {
                    stateRed.put(s, true);
                } finally {
                	redLock.unlock();
                }
            } else if (s.isAccepting()) {
            	countLock.lock();
            	try {
	                int count = stateCount.get(s);
	                stateCount.put(s, count + 1);
            	} finally {
            		countLock.unlock();
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


    public void init(int nrOfThreads) {
    	for (int i = 1; i <= nrOfThreads; i++) {
    		super.swarm.add(new Bird(i));
    	}
    }

}
