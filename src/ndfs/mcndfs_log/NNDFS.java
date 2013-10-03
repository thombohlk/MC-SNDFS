package ndfs.mcndfs_log;

import graph.State;
import helperClasses.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.CycleFound;
import ndfs.Result;

public class NNDFS extends MCNDFS {

    protected Logger logger;
    protected ArrayList<Bird> swarm;

    class Bird extends GeneralBird {

        Bird(int id, File file) {
			super(id, file);
		}

		protected void dfsRed(State s) throws Result, InterruptedException {
        	logger.logDfsRedStart(id, s);
        	
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
                	logger.logIncrementWait();
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notifyAll();
	                logger.logDecrementWait();
                }
            }

            synchronized (stateRed) {
                stateRed.put(s, true);
            }
            localStatePink.put(s, false);

            logger.logDfsRedDone();
        }


        protected void dfsBlue(State s) throws Result, InterruptedException {
        	logger.logDfsBlueStart(id, s);
        	
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

            logger.logDfsBlueDone();
        }

    }

    
    public NNDFS(File file) {
    	super(file);
        
        logger = new Logger(file);
    }
    
    public void tearDown() {
        logger.stop();
        logger.printLogs();
    }

    protected void nndfs() throws Result {
        logger.start();
        super.nndfs();
    }

	@Override
	public void init(int nrOfThreads) {
    	for (int i = 1; i <= nrOfThreads; i++) {
    		super.swarm.add(new Bird(i, file));
    	}
	}

}
